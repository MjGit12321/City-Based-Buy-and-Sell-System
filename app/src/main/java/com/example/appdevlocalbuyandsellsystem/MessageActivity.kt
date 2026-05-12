package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FieldValue

class MessageActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.messagepage)

        // Retrieve Chat Info from Intent
        chatId = intent.getStringExtra("CHAT_ID")
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        findViewById<TextView>(R.id.tvUserName).text = userName

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        adapter = ChatAdapter(chatMessages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        rvChat.layoutManager = layoutManager
        rvChat.adapter = adapter

        // Listen for Messages
        listenForMessages()

        // Setup Send Button
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty() && chatId != null) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }

        // Back Button
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
    }

    private fun listenForMessages() {
        val id = chatId ?: return

        db.collection("chats").document(id).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    chatMessages.clear()
                    for (doc in snapshots) {
                        val text = doc.getString("message") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val time = doc.getTimestamp("timestamp")?.toDate()?.let {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
                        } ?: "Now"

                        val type = if (senderId == auth.currentUser?.uid) {
                            MessageType.SENT
                        } else {
                            MessageType.RECEIVED
                        }

                        chatMessages.add(ChatMessage(text, time, type))
                    }
                    adapter.notifyDataSetChanged()
                    findViewById<RecyclerView>(R.id.rvChat).scrollToPosition(chatMessages.size - 1)
                }
            }
    }

    private fun sendMessage(text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val id = chatId ?: return

        val messageData = hashMapOf(
            "message" to text,
            "senderId" to currentUserId,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // 1. Add message to sub-collection
        db.collection("chats").document(id).collection("messages").add(messageData)

        // 2. Update the main chat document for the Inbox preview
        val chatUpdate = hashMapOf(
            "lastMessage" to text,
            "lastTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "lastTime" to SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )
        db.collection("chats").document(id).update(chatUpdate as Map<String, Any>)
    }
}
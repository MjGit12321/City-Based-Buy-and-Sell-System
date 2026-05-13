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
    private var otherUserId: String? = null
    private var otherUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.messagepage)

        // Retrieve Chat Info from Intent
        chatId = intent.getStringExtra("CHAT_ID")
        otherUserId = intent.getStringExtra("OTHER_USER_ID")
        otherUserName = intent.getStringExtra("USER_NAME") ?: "User"
        findViewById<TextView>(R.id.tvUserName).text = otherUserName

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

        // Click User in Header -> View Profile
        val headerProfile = findViewById<ImageView>(R.id.ivHeaderProfile)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        
        val goToProfile = {
            if (!otherUserId.isNullOrEmpty()) {
                val intent = Intent(this, ViewOtherUserProfileActivity::class.java)
                intent.putExtra("SELLER_ID", otherUserId)
                intent.putExtra("SELLER_NAME", otherUserName)
                startActivity(intent)
            }
        }
        
        headerProfile.setOnClickListener { goToProfile() }
        tvUserName.setOnClickListener { goToProfile() }

        // Call Button
        findViewById<ImageView>(R.id.ivCall).setOnClickListener {
            val intent = Intent(this, CallingActivity::class.java)
            intent.putExtra("USER_NAME", otherUserName)
            startActivity(intent)
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
            finish() // Since we're coming from a specific chat, going to Inbox should probably close this
        }
    }

    private fun listenForMessages() {
        val id = chatId ?: return

        db.collection("chats").document(id).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null) {
                    chatMessages.clear()
                    var lastDateLabel: String? = null
                    val dateHeaderFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

                    for (doc in snapshots) {
                        val text = doc.getString("message") ?: ""
                        val senderId = doc.getString("senderId") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")?.toDate()

                        val dateLabel = timestamp?.let { dateHeaderFormatter.format(it) } ?: "Today"
                        val timeLabel = timestamp?.let { timeFormatter.format(it) } ?: "Now"

                        // Add date separator if date changed
                        if (dateLabel != lastDateLabel) {
                            chatMessages.add(ChatMessage("", dateLabel, MessageType.TIMESTAMP))
                            lastDateLabel = dateLabel
                        }

                        val type = if (senderId == auth.currentUser?.uid) {
                            MessageType.SENT
                        } else {
                            MessageType.RECEIVED
                        }

                        chatMessages.add(ChatMessage(text, timeLabel, type))
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

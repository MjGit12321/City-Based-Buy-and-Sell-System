package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class InboxActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var inboxAdapter: InboxAdapter
    private val conversationList = mutableListOf<InboxMessage>()
    private lateinit var rvInbox: RecyclerView
    private lateinit var tvNoMessages: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.inboxpage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Views
        rvInbox = findViewById(R.id.rvInbox)
        tvNoMessages = findViewById(R.id.tvNoMessages)

        // Adjust for system bars
        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        rvInbox.layoutManager = LinearLayoutManager(this)
        inboxAdapter = InboxAdapter(conversationList) { conversation ->
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("CHAT_ID", conversation.originalDocId)
            intent.putExtra("USER_NAME", conversation.name)
            startActivity(intent)
        }
        rvInbox.adapter = inboxAdapter

        loadConversations()
        setupNavigation()
    }

    private fun loadConversations() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreError", "Error fetching chats: ${e.message}")
                    return@addSnapshotListener
                }

                conversationList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (doc in snapshots) {
                        // Extract the "other" user's name from the 'names' map
                        val namesMap = doc.get("names") as? Map<String, String>
                        val otherUserName = namesMap?.filterKeys { it != currentUserId }?.values?.firstOrNull() ?: "User"

                        val lastMsg = doc.getString("lastMessage") ?: ""
                        val time = doc.getString("lastTime") ?: ""
                        val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0

                        val message = InboxMessage(doc.id.hashCode(), otherUserName, lastMsg, time, unreadCount)
                        message.originalDocId = doc.id
                        conversationList.add(message)
                    }
                    tvNoMessages.visibility = View.GONE
                    rvInbox.visibility = View.VISIBLE
                } else {
                    tvNoMessages.visibility = View.VISIBLE
                    rvInbox.visibility = View.GONE
                }
                inboxAdapter.notifyDataSetChanged()
            }
    }

    private fun setupNavigation() {
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
            // Already here
        }
    }
}

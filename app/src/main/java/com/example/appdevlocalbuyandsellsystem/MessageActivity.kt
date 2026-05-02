package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.messagepage)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup Back Button
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.ivCall).setOnClickListener {
            startActivity(Intent(this, CallingActivity::class.java))
        }

        // Setup RecyclerView
        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val userName = intent.getStringExtra("USER_NAME") ?: "Luminosity"
        
        // Update header name
        findViewById<android.widget.TextView>(R.id.tvUserName).text = userName

        val chatMessages = mutableListOf<ChatMessage>()
        
        // Generate a fake conversation based on the user name
        when (userName) {
            "Luminosity" -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Yesterday", MessageType.TIMESTAMP),
                    ChatMessage("Is the camera still for sale?", "9:00pm", MessageType.RECEIVED),
                    ChatMessage("Yes, it is! Are you interested?", "9:05pm", MessageType.SENT),
                    ChatMessage("I am! Is there any room for negotiation?", "9:10pm", MessageType.RECEIVED),
                    ChatMessage("A little bit, what's your offer?", "9:15pm", MessageType.SENT)
                ))
            }
            "Soppe" -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Today at 8:30am", MessageType.TIMESTAMP),
                    ChatMessage("Hey, are you free to meet up today?", "8:31am", MessageType.RECEIVED),
                    ChatMessage("Yes, I can meet you tomorrow at the mall.", "8:35am", MessageType.SENT),
                    ChatMessage("That works for me. Which mall?", "8:36am", MessageType.RECEIVED)
                ))
            }
            "Janahn" -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Yesterday", MessageType.TIMESTAMP),
                    ChatMessage("The bike is in great condition!", "10:00am", MessageType.RECEIVED),
                    ChatMessage("That's great to hear. Can I see it?", "10:05am", MessageType.SENT)
                ))
            }
            "Mags" -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Tuesday", MessageType.TIMESTAMP),
                    ChatMessage("Thanks for the discount!", "2:00pm", MessageType.RECEIVED),
                    ChatMessage("No problem, glad you like it!", "2:05pm", MessageType.SENT)
                ))
            }
            "Cisis" -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Monday", MessageType.TIMESTAMP),
                    ChatMessage("Where is the pickup location?", "11:00am", MessageType.RECEIVED),
                    ChatMessage("Near the central station.", "11:10am", MessageType.SENT)
                ))
            }
            else -> {
                chatMessages.addAll(listOf(
                    ChatMessage("", "Recently", MessageType.TIMESTAMP),
                    ChatMessage("Hello there!", "Now", MessageType.RECEIVED)
                ))
            }
        }

        val adapter = ChatAdapter(chatMessages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true // Start filling the list from the bottom
        rvChat.layoutManager = layoutManager
        rvChat.adapter = adapter

        // Setup Send Button
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                chatMessages.add(ChatMessage(text, "Just now", MessageType.SENT))
                adapter.notifyItemInserted(chatMessages.size - 1)
                rvChat.scrollToPosition(chatMessages.size - 1)
                etMessage.text.clear()
            }
        }

        // Scroll to bottom when keyboard appears
        rvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rvChat.postDelayed({
                    if (chatMessages.isNotEmpty()) {
                        rvChat.smoothScrollToPosition(chatMessages.size - 1)
                    }
                }, 100)
            }
        }

        // Navigation bar logic
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

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
    }
}
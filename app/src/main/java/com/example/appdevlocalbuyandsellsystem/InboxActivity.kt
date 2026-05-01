package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InboxActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.inboxpage)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        val rvInbox = findViewById<RecyclerView>(R.id.rvInbox)
        val conversations = listOf(
            InboxMessage(1, "Luminosity", "Is the camera still for sale?", "7/26", 1),
            InboxMessage(2, "Soppe", "I can meet you tomorrow at the mall.", "4/28", 4),
            InboxMessage(3, "Janahn", "The bike is in great condition!", "Yesterday", 0),
            InboxMessage(4, "Mags", "Thanks for the discount!", "Tuesday", 0),
            InboxMessage(5, "Cisis", "Where is the pickup location?", "Monday", 2),
            InboxMessage(6, "Jade", "I sent the payment already.", "Sunday", 0)
        )

        rvInbox.layoutManager = LinearLayoutManager(this)
        rvInbox.adapter = InboxAdapter(conversations) { conversation ->
            // Open the specific chat screen
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("USER_NAME", conversation.name)
            startActivity(intent)
        }

        // Navigation Bar Logic
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
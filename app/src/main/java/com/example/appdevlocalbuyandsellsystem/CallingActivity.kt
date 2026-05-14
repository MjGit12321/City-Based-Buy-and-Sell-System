package com.example.appdevlocalbuyandsellsystem

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File


class CallingActivity : AppCompatActivity() {

    private lateinit var tvCallTimer: TextView
    private var secondsElapsed = 0
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            secondsElapsed++
            val minutes = secondsElapsed / 60
            val seconds = secondsElapsed % 60
            tvCallTimer.text = String.format("%02d:%02d", minutes, seconds)
            handler.postDelayed(this, 1000)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.calling_screen)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvCallTimer = findViewById(R.id.tvCallTimer)
        
        // Get User Info from Intent
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        val otherUserId = intent.getStringExtra("OTHER_USER_ID")
        
        findViewById<TextView>(R.id.tvCallerName).text = userName

        // Load Caller Profile Image
        if (!otherUserId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(otherUserId).get()
                .addOnSuccessListener { doc ->
                    val profileImg = doc.getString("profileImageUrl")
                    if (!profileImg.isNullOrEmpty()) {
                        val file = if (profileImg.contains("/")) File(profileImg) else File(filesDir, profileImg)
                        Glide.with(this)
                            .load(file)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user)
                            .into(findViewById<ImageView>(R.id.ivCallerProfile))
                    }
                }
        }

        // Setup Button Listeners
        findViewById<ImageButton>(R.id.btnMute).setOnClickListener {
            Toast.makeText(this, "Microphone Muted", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnRecord).setOnClickListener {
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnPause).setOnClickListener {
            Toast.makeText(this, "Call Paused", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnEndCall).setOnClickListener {
            Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Start the call timer
        handler.postDelayed(timerRunnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the timer when activity is destroyed
        handler.removeCallbacks(timerRunnable)
    }
}

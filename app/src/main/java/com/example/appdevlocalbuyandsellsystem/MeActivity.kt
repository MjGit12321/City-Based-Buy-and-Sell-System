package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class MeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.mepage)

        // 1. INITIALIZE FIREBASE (Crucial)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 2. CALL THE LOAD FUNCTION (Crucial)
        loadUserData()

        // Handle System Bar Insets
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupButtons()
        setupNavigation()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val fullname = snapshot.getString("fullName") ?: ""
                val contact = snapshot.getString("contactNumber") ?: ""
                val altContact = snapshot.getString("altContactNumber") ?: ""
                val hobbies = snapshot.getString("hobbies") ?: ""

                val brgy = snapshot.getString("barangay") ?: ""
                val city = snapshot.getString("city") ?: ""
                val prov = snapshot.getString("province") ?: ""
                val reg = snapshot.getString("region") ?: ""

                val fullAddress = if (brgy.isNotEmpty()) "$brgy, $city, $prov, $reg" else "Address not set"

                // UI UPDATES
                findViewById<TextView>(R.id.tvMeNameLarge).text = fullname
                findViewById<TextView>(R.id.tvMeFullName).text = "Full Name: $fullname"
                findViewById<TextView>(R.id.tvMeContact).text = "Contact: $contact"
                findViewById<TextView>(R.id.tvMeAltContact).text = "Alt Contact: $altContact"
                findViewById<TextView>(R.id.tvMeAddress).text = "Address: $fullAddress"

                // FIXED: Changed ID from tvMeAddress to tvMeHobbies
                findViewById<TextView>(R.id.tvMeHobbies).text = "Hobbies: $hobbies"

                val profileImgUrl = snapshot.getString("profileImageUrl")
                if (!profileImgUrl.isNullOrEmpty() && !isFinishing && !isDestroyed) {
                    val file = if (profileImgUrl.contains("/")) File(profileImgUrl) else File(filesDir, profileImgUrl)
                    Glide.with(this@MeActivity)
                        .load(file)
                        .circleCrop()
                        .into(findViewById<ImageView>(R.id.ivMeProfileLarge))
                }
            }
    }

    private fun setupButtons() {
        findViewById<CardView>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        findViewById<CardView>(R.id.btnMyProducts).setOnClickListener {
            startActivity(Intent(this, MyProductsActivity::class.java))
        }
        findViewById<CardView>(R.id.btnUploadProduct).setOnClickListener {
            startActivity(Intent(this, UploadProductActivity::class.java))
        }
        findViewById<CardView>(R.id.btnLogoutMe).setOnClickListener {
            auth.signOut() // Sign out from Firebase
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }
}
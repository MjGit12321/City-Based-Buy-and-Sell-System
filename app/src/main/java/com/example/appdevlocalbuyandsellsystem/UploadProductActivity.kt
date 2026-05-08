package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// 1. IMPORT FIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class UploadProductActivity : AppCompatActivity() {

    // 2. DECLARE FIREBASE
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.upload_product)

        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // References to your EditTexts (Ensure these IDs match your XML)
        val etProductName = findViewById<EditText>(R.id.etProductName)
        val etProductPrice = findViewById<EditText>(R.id.etPrice)
        val etProductDescription = findViewById<EditText>(R.id.etProductDesc)
        val etBarangay = findViewById<EditText>(R.id.etBarangay)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etProvince = findViewById<EditText>(R.id.etProvince)

        findViewById<ConstraintLayout>(R.id.containerUploadImage).setOnClickListener {
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show()
        }

        // 3. UPDATED UPLOAD BUTTON LOGIC
        findViewById<Button>(R.id.btnUploadSubmit).setOnClickListener {
            val name = etProductName.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val description = etProductDescription.text.toString().trim()
            val baranggay = etBarangay.text.toString().trim()
            val city = etCity.text.toString().trim()
            val province = etProvince.text.toString().trim()
            val currentUserId = auth.currentUser?.uid

            if (name.isNotEmpty() && price.isNotEmpty() && currentUserId != null) {

                // Create data map
                val product = hashMapOf(
                    "name" to name,
                    "price" to price,
                    "description" to description,
                    "baranggay" to baranggay,
                    "city" to city,
                    "province" to province,
                    "sellerId" to currentUserId,
                    "username" to (auth.currentUser?.displayName ?: "Unknown Seller"),
                    "rating" to 0.0f,
                    "timestamp" to FieldValue.serverTimestamp() // For "Newest First" sorting
                )

                // 4. SAVE TO FIRESTORE
                db.collection("products")
                    .add(product)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Product Uploaded Successfully!", Toast.LENGTH_LONG).show()

                        val intent = Intent(this, MainpageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation Bar Listeners
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

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
            finish()
        }
    }
}
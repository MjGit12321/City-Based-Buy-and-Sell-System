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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class UploadProductActivity : AppCompatActivity() {

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

        val etProductName = findViewById<EditText>(R.id.etProductName)
        val etProductPrice = findViewById<EditText>(R.id.etPrice)
        val etProductDescription = findViewById<EditText>(R.id.etProductDesc)

        findViewById<ConstraintLayout>(R.id.containerUploadImage).setOnClickListener {
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show()
        }

        // UPDATED UPLOAD BUTTON LOGIC
        findViewById<Button>(R.id.btnUploadSubmit).setOnClickListener {
            val name = etProductName.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val description = etProductDescription.text.toString().trim()
            val currentUserId = auth.currentUser?.uid

            if (name.isNotEmpty() && price.isNotEmpty() && currentUserId != null) {

                // 1. Fetch the User's profile data first to get Name and Location
                db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            // 2. Extract profile details
                            val sellerName = snapshot.getString("fullName") ?: "Unknown Seller"
                            val brgy = snapshot.getString("barangay") ?: ""
                            val city = snapshot.getString("city") ?: ""
                            val prov = snapshot.getString("province") ?: ""
                            val reg = snapshot.getString("region") ?: ""

                            // Create the formatted address string
                            val sellerLocation = if (brgy.isNotEmpty()) {
                                "$brgy, $city, $prov, $reg"
                            } else {
                                "Location not set"
                            }

                            // 3. Create the product data map with profile info
                            val product = hashMapOf(
                                "name" to name,
                                "price" to price,
                                "description" to description,
                                "sellerId" to currentUserId,
                                "sellerName" to sellerName,
                                "location" to sellerLocation,
                                "brgy" to brgy,
                                "city" to city,
                                "prov" to prov,
                                "reg" to reg,
                                "timestamp" to FieldValue.serverTimestamp()
                            )

                            // 4. Save to "products" collection
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
                            Toast.makeText(this, "User profile not found. Please complete your profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation Bar Listeners
        setupNavigation()
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

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
            finish()
        }
    }
}
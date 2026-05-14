package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.util.Log

class UploadProductActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var progressDialog: android.app.ProgressDialog

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val ivPlaceholder = findViewById<ImageView>(R.id.ivPlaceholder)
            ivPlaceholder.setImageURI(uri)
            ivPlaceholder.scaleType = ImageView.ScaleType.CENTER_CROP
            // Ensure any tints are removed so the image shows properly
            ivPlaceholder.imageTintList = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.upload_product)

        progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Uploading Product...")
            setCancelable(false)
        }

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
            pickImageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnUploadSubmit).setOnClickListener {
            val name = etProductName.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val description = etProductDescription.text.toString().trim()
            val currentUserId = auth.currentUser?.uid

            if (name.isNotEmpty() && price.isNotEmpty() && currentUserId != null) {
                progressDialog.show()
                db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val sellerName = snapshot.getString("fullName") ?: "Unknown Seller"
                            val brgy = snapshot.getString("barangay") ?: ""
                            val city = snapshot.getString("city") ?: ""
                            val prov = snapshot.getString("province") ?: ""
                            val reg = snapshot.getString("region") ?: ""

                            val sellerLocation = if (brgy.isNotEmpty()) {
                                "$brgy, $city, $prov, $reg"
                            } else {
                                "Location not set"
                            }

                            if (selectedImageUri != null) {
                                val localPath = saveImageToInternalStorage(selectedImageUri!!)
                                if (localPath != null) {
                                    saveProductToFirestore(name, price, description, currentUserId, sellerName, sellerLocation, brgy, city, prov, reg, localPath)
                                } else {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Failed to save image locally.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                saveProductToFirestore(name, price, description, currentUserId, sellerName, sellerLocation, brgy, city, prov, reg, "")
                            }
                        } else {
                            Toast.makeText(this, "User profile incomplete.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
            }
        }

        setupNavigation()
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "prod_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            // Fix: Store only the filename so other users on the same device can find it
            fileName
        } catch (e: Exception) {
            Log.e("LocalSave", "Error: ${e.message}")
            null
        }
    }

    private fun saveProductToFirestore(
        name: String, price: String, description: String,
        userId: String, sellerName: String, location: String,
        brgy: String, city: String, prov: String, reg: String,
        imageUrl: String
    ) {
        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "sellerId" to userId,
            "sellerName" to sellerName,
            "location" to location,
            "baranggay" to brgy,
            "city" to city,
            "province" to prov,
            "region" to reg,
            "imageUrl" to imageUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("products")
            .add(product)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Product Uploaded Successfully!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainpageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
            finish()
        }
    }
}

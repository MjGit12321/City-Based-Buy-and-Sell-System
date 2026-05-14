package com.example.appdevlocalbuyandsellsystem

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null
    private lateinit var ivProfilePic: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            if (!isFinishing && !isDestroyed) {
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(ivProfilePic)
            }
        }
    }
    private fun setupStaticSpinners(spinnerSex: Spinner) {
        val genders = listOf("Male", "Female")

        // 1. Initialize the adapter with the custom black text layout
        val adapter = ArrayAdapter(this, R.layout.spinner_item_black, genders)

        // 2. Set the dropdown resource to use the same black text layout
        adapter.setDropDownViewResource(R.layout.spinner_item_black)

        spinnerSex.adapter = adapter
    }

    private fun setupAddressHierarchy(reg: Spinner, prov: Spinner, city: Spinner, brgy: Spinner) {
        val regions = listOf("Region X (Northern Mindanao)", "Region XI (Davao Region)", "Region IX (Zamboanga Peninsula)")

        // Create the base adapter using the custom black-text layout
        val regionAdapter = ArrayAdapter(this, R.layout.spinner_item_black, regions)
        regionAdapter.setDropDownViewResource(R.layout.spinner_item_black)
        reg.adapter = regionAdapter

        reg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val provinces = when (pos) {
                    0 -> listOf("Misamis Oriental", "Bukidnon", "Lanao del Norte")
                    1 -> listOf("Davao del Sur", "Davao del Norte", "Davao de Oro")
                    2 -> listOf("Zamboanga del Sur", "Zamboanga del Norte")
                    else -> listOf("Select Province")
                }
                val provAdapter = ArrayAdapter(this@EditProfileActivity, R.layout.spinner_item_black, provinces)
                provAdapter.setDropDownViewResource(R.layout.spinner_item_black)
                prov.adapter = provAdapter
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        prov.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val selectedProv = prov.selectedItem.toString()
                val cities = when (selectedProv) {
                    "Misamis Oriental" -> listOf("Cagayan de Oro", "Gingoog", "El Salvador")
                    "Bukidnon" -> listOf("Malaybalay", "Valencia")
                    "Davao del Sur" -> listOf("Davao City", "Digos")
                    "Zamboanga del Sur" -> listOf("Pagadian", "Zamboanga City")
                    else -> listOf("Other City")
                }
                val cityAdapter = ArrayAdapter(this@EditProfileActivity, R.layout.spinner_item_black, cities)
                cityAdapter.setDropDownViewResource(R.layout.spinner_item_black)
                city.adapter = cityAdapter
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        city.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                val selectedCity = city.selectedItem.toString()
                val barangays = when (selectedCity) {
                    "Cagayan de Oro" -> listOf("Nazareth", "Carmen", "Balulang", "Lapasan")
                    "Davao City" -> listOf("Buhangin", "Talomo", "Agdao")
                    "Zamboanga City" -> listOf("Tetuan", "Guiwan", "Pasonanca")
                    else -> listOf("Poblacion", "Barangay 1", "Barangay 2")
                }

                // CRITICAL FIX: You must use R.layout.spinner_item_black here too!
                val brgyAdapter = ArrayAdapter(this@EditProfileActivity, R.layout.spinner_item_black, barangays)

                // This makes the popup list items black
                brgyAdapter.setDropDownViewResource(R.layout.spinner_item_black)

                brgy.adapter = brgyAdapter
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_profile)

        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. UI REFERENCES
        val etFullName = findViewById<EditText>(R.id.etEditFacebook)
        val etBirthdate = findViewById<EditText>(R.id.etBirthdate)
        val etContact = findViewById<EditText>(R.id.etEditContact)
        val etAltContact = findViewById<EditText>(R.id.etEditAltContact)
        val etHobbies = findViewById<EditText>(R.id.etEditHobbies)
        ivProfilePic = findViewById(R.id.ivEditProfilePic)
        val btnChangeImage = findViewById<LinearLayout>(R.id.btnChangeImage)

        // Spinners
        val spinnerSex = findViewById<Spinner>(R.id.spinnerSex)
        val spinnerRegion = findViewById<Spinner>(R.id.spinnerRegion)
        val spinnerProvince = findViewById<Spinner>(R.id.spinnerProvince)
        val spinnerCity = findViewById<Spinner>(R.id.spinnerCity)
        val spinnerBaranggay = findViewById<Spinner>(R.id.spinnerBaranggay)

        btnChangeImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        setupStaticSpinners(spinnerSex)
        setupAddressHierarchy(spinnerRegion, spinnerProvince, spinnerCity, spinnerBaranggay)

        // 2. LOAD EXISTING DATA (Updated to include new fields)
        loadUserProfile(etFullName, etBirthdate, etContact, etAltContact, etHobbies)

        etBirthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etBirthdate.setText("${month + 1}/$day/$year")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 3. UPDATED SAVE LOGIC
        findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            val name = etFullName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sex = spinnerSex.selectedItem?.toString() ?: ""
            val region = spinnerRegion.selectedItem?.toString() ?: ""
            val province = spinnerProvince.selectedItem?.toString() ?: ""
            val city = spinnerCity.selectedItem?.toString() ?: ""
            val barangay = spinnerBaranggay.selectedItem?.toString() ?: ""

            val userData = hashMapOf<String, Any>(
                "fullName" to name,
                "birthdate" to etBirthdate.text.toString().trim(),
                "contactNumber" to etContact.text.toString().trim(),
                "altContactNumber" to etAltContact.text.toString().trim(),
                "hobbies" to etHobbies.text.toString().trim(),
                "sex" to sex,
                "region" to region,
                "province" to province,
                "city" to city,
                "barangay" to barangay,
                "uid" to (auth.currentUser?.uid ?: "")
            )

            if (selectedImageUri != null) {
                val localPath = saveImageToInternalStorage(selectedImageUri!!)
                if (localPath != null) {
                    userData["profileImageUrl"] = localPath
                }
            }

            saveUserProfile(userData)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "profile_${auth.currentUser?.uid}.jpg"
            val file = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            // Instead of absolute path, we save the filename
            // This ensures other accounts on the same device can find it in their own filesDir
            fileName
        } catch (e: Exception) {
            Log.e("LocalSave", "Error: ${e.message}")
            null
        }
    }

    private fun loadUserProfile(etName: EditText, etDob: EditText, etContact: EditText, etAlt: EditText, etHobbies: EditText) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etName.setText(doc.getString("fullName"))
                etDob.setText(doc.getString("birthdate"))
                etContact.setText(doc.getString("contactNumber"))
                etAlt.setText(doc.getString("altContactNumber"))
                etHobbies.setText(doc.getString("hobbies"))
                
                val imgUrl = doc.getString("profileImageUrl")
                if (!imgUrl.isNullOrEmpty() && !isFinishing && !isDestroyed) {
                    val file = if (imgUrl.contains("/")) File(imgUrl) else File(filesDir, imgUrl)
                    Glide.with(this)
                        .load(file)
                        .circleCrop()
                        .into(ivProfilePic)
                }
            }
        }
    }

    private fun saveUserProfile(data: HashMap<String, Any>) {
        val uid = auth.currentUser?.uid ?: return
        
        // Add completion flag
        data["profileCompleted"] = "true"

        db.collection("users").document(uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                
                // Go to Mainpage after profile completion
                val intent = Intent(this, MainpageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ... (Keep setupStaticSpinners and setupAddressHierarchy from previous step)
}
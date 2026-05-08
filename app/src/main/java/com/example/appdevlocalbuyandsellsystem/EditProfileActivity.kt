package com.example.appdevlocalbuyandsellsystem

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.edit_profile)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Change Image Click Listener
        findViewById<LinearLayout>(R.id.btnChangeImage).setOnClickListener {
            Toast.makeText(this, "Opening Gallery...", Toast.LENGTH_SHORT).show()
        }

        // Date Picker Logic
        val etBirthdate = findViewById<EditText>(R.id.etBirthdate)
        etBirthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                etBirthdate.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        // Update Profile Logic
        findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            // In a real app, you would save data here.
            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Navigation Bar Logic
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

        // Already on Me page path
        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            val intent = Intent(this, MeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}

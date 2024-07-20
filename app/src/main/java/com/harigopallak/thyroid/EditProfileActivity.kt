package com.harigopallak.thyroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var saveButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        saveButton = findViewById(R.id.save_button)

        firebaseAuth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = firebaseAuth.currentUser

        user?.let {
            editName.setText(it.displayName)
            editEmail.setText(it.email)
        }

        saveButton.setOnClickListener {
            val name = editName.text.toString()
            val email = editEmail.text.toString()

            // Update user profile
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }

            user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            user?.updateEmail(email)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

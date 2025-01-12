package pt.ua.androidproj

import  android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var loginButton: Button

    // Firebase Authentication instance
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Bind views
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        loginButton = findViewById(R.id.loginButton)

        // Toggle password visibility
        passwordVisibilityToggle.setOnClickListener {
            val inputType = if (passwordEditText.inputType == 129) {
                1 // Visible text
            } else {
                129 // Hidden text
            }
            passwordEditText.inputType = inputType
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Login button logic
        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Authenticate with Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Successful login
                        val intent = Intent(this, HelloWorldActivity::class.java)

                        // Pass the user's email to the next activity
                        val currentUser = firebaseAuth.currentUser
                        currentUser?.let {
                            intent.putExtra("uid", it.uid)
                        }

                        startActivity(intent)
                        finish()
                    } else {
                        // Login failed
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

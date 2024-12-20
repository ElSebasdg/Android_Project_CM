package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var loginButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar o Firebase
        FirebaseApp.initializeApp(this)

        // Conectar o layout ao código
        setContentView(R.layout.activity_main)

        // Vincular elementos do layout aos componentes Kotlin
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle)
        loginButton = findViewById(R.id.loginButton)

        // Alternar visibilidade da senha
        passwordVisibilityToggle.setOnClickListener {
            val inputType = if (passwordEditText.inputType == 129) {
                1 // Texto visível
            } else {
                129 // Texto oculto
            }
            passwordEditText.inputType = inputType
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString() // Assuming username is email
            val password = passwordEditText.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, HelloWorldActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
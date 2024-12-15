package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Configurar a lógica do botão de login
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username == "admin" && password == "admin") {
                // Login bem-sucedido: navegar para HelloWorldActivity
                val intent = Intent(this, HelloWorldActivity::class.java)
                startActivity(intent)
                finish() // Fecha a MainActivity
            } else {
                // Login inválido: exibir mensagem de erro
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
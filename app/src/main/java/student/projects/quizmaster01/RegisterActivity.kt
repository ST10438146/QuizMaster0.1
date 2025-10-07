package student.projects.quizmaster01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.etRegUsername)
        val emailInput = findViewById<EditText>(R.id.etRegEmail)
        val passwordInput = findViewById<EditText>(R.id.etRegPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val goToLoginButton = findViewById<Button>(R.id.btn_login_tab_go)

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        // ✅ Create Firestore document for this user
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "xp" to 0,
                            "coins" to 100,  // give starter coins
                            "activeQuestsCount" to 0
                        )

                        firestore.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Welcome, $username!", Toast.LENGTH_SHORT).show()
                                navigateToDashboard()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "User created, but Firestore failed: ${e.message}", Toast.LENGTH_LONG).show()
                                navigateToDashboard()
                            }
                    } else {
                        Toast.makeText(this, "Registration failed — UID missing", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
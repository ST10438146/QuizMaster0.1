package student.projects.quizmaster01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class AuthActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val isAuthenticated = false // TODO hook up Firebase auth state

        if (isAuthenticated) {
            navigateToMainApp()
        } else {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment
            navController = navHostFragment.navController
        }
    }

    fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
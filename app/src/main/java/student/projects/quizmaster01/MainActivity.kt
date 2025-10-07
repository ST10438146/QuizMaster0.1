package student.projects.quizmaster01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity is the single activity that hosts the main application screens (Home/Play, Leaderboards, Settings).
 * It manages the navigation using a BottomNavigationView, consistent with the QuizMaster UI design.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout containing the FrameLayout (container) and BottomNavigationView
        // This corresponds to the structure shown in 'QuizMaster UI.pdf'
        setContentView(R.layout.activity_main)

        // 1. Find the NavHostFragment that manages the fragment switching
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.home_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // 2. Link the BottomNavigationView (from the UI PDF) to the NavController
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)


        // The main navigation destinations must correspond to the menu items:
        // R.id.nav_play -> Home/Play screen
        // R.id.nav_leaderboards -> Leaderboards screen (Milestone 5)
        // R.id.nav_settings -> Settings screen (Milestone 1)
        // R.id.nav_chatbot -> Chatbot screen (Milestone 6)
    }
}
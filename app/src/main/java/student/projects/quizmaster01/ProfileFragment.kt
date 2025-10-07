package student.projects.quizmaster01

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvUserXP: TextView
    private lateinit var tvBadges: TextView
    private lateinit var switchBiometrics: Switch
    private lateinit var switchNotifications: Switch
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Bind views
        tvUserXP = view.findViewById(R.id.tvUserXP)
        tvBadges = view.findViewById(R.id.tvBadges)
        switchBiometrics = view.findViewById(R.id.switchBiometrics)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        btnLogout = view.findViewById(R.id.btnLogout)

        setupObservers()
        setupToggles()
        setupLogout()

        return view
    }

    private fun setupObservers() {
        // Observe XP, coins, etc. from ViewModel
        viewModel.userStatus.observe(viewLifecycleOwner) { status ->
            tvUserXP.text = "XP: ${status.xp} (Level ${calculateLevel(status.xp)})"
            tvBadges.text = "Badges Earned: ${calculateBadges(status.xp)}/20"
        }

        // Also load user preferences from Firestore
        loadUserPreferences()
    }

    private fun setupToggles() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Load saved states first (fallback for offline)
        switchBiometrics.isChecked = prefs.getBoolean("biometric_enabled", false)
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)

        // Save + sync Biometric preference
        switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("biometric_enabled", isChecked).apply()
            updateUserPreference("biometricEnabled", isChecked)
            Toast.makeText(requireContext(),
                if (isChecked) "Biometric login enabled" else "Biometric login disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Save + sync Notifications preference
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            updateUserPreference("notificationsEnabled", isChecked)
            Toast.makeText(requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /** Loads preferences from Firestore (remote sync) */
    private fun loadUserPreferences() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val bioEnabled = doc.getBoolean("biometricEnabled") ?: false
                val notifEnabled = doc.getBoolean("notificationsEnabled") ?: true

                switchBiometrics.isChecked = bioEnabled
                switchNotifications.isChecked = notifEnabled

                // Save locally too (offline cache)
                val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("biometric_enabled", bioEnabled)
                    .putBoolean("notifications_enabled", notifEnabled)
                    .apply()
            }
    }

    /** Updates Firestore when toggle switches change */
    private fun updateUserPreference(field: String, value: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        val updateData = mapOf(field to value)
        firestore.collection("users").document(uid)
            .set(updateData, SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to sync setting", Toast.LENGTH_SHORT).show()
            }
    }

    // XP → Level and Badges (for display only)
    private fun calculateLevel(xp: Int): Int = (xp / 1000) + 1
    private fun calculateBadges(xp: Int): Int = (xp / 2000).coerceAtMost(20)
}
package student.projects.quizmaster01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvUserName: TextView
    private lateinit var tvXPValue: TextView
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnStartSolo: Button
    private lateinit var btnJoinMatch: Button
    private lateinit var btnCreateMatch: Button
    private lateinit var btnNotifications: ImageButton

    private val categories = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_play, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Bind views
        tvUserName = view.findViewById(R.id.tvUserName)
        tvXPValue = view.findViewById(R.id.tvXPValue)
        xpProgressBar = view.findViewById(R.id.xpProgressBar)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        btnStartSolo = view.findViewById(R.id.btnStartSolo)
        btnJoinMatch = view.findViewById(R.id.btnJoinMatch)
        btnCreateMatch = view.findViewById(R.id.btnCreateMatch)
        btnNotifications = view.findViewById(R.id.btnNotifications)

        setupCategorySpinner()
        observeUserStatus()
        setupListeners()

        return view
    }

    private fun setupCategorySpinner() {
        categories.clear()
        categories.addAll(
            listOf("General Knowledge", "Science", "History", "Sports", "Entertainment")
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = adapter
    }

    private fun observeUserStatus() {
        viewModel.userStatus.observe(viewLifecycleOwner) { status ->
            tvUserName.text = "Welcome, ${status.playerName}"
            val progress = (status.xp % 100)
            xpProgressBar.progress = progress
            tvXPValue.text = "XP: ${status.xp}/100 to next level"
        }
    }

    private fun setupListeners() {
        btnStartSolo.setOnClickListener {
            val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "General Knowledge"

            Toast.makeText(
                requireContext(),
                "Starting Solo Game in $selectedCategory!",
                Toast.LENGTH_SHORT
            ).show()

            // ✅ Manual bundle passing (works even if SafeArgs not active)
            val bundle = Bundle().apply {
                putString("category", selectedCategory)
            }

            // Navigate to GameFragment
            findNavController().navigate(R.id.gameFragment, bundle)
        }

        btnJoinMatch.setOnClickListener {
            Toast.makeText(requireContext(), "Joining a random match...", Toast.LENGTH_SHORT).show()
        }

        btnCreateMatch.setOnClickListener {
            Toast.makeText(requireContext(), "Creating a private room...", Toast.LENGTH_SHORT).show()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "No new notifications!", Toast.LENGTH_SHORT).show()
        }
    }
}

package student.projects.quizmaster01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameOverFragment : Fragment() {

    private lateinit var tvFinalScore: TextView
    private lateinit var tvCategoryPlayed: TextView
    private lateinit var tvXpEarned: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnBackHome: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game_over, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvFinalScore = view.findViewById(R.id.tvFinalScore)
        tvCategoryPlayed = view.findViewById(R.id.tvCategoryPlayed)
        tvXpEarned = view.findViewById(R.id.tvXpEarned)
        btnRetry = view.findViewById(R.id.btnRetry)
        btnBackHome = view.findViewById(R.id.btnBackHome)

        val score = arguments?.getInt("score") ?: 0
        val category = arguments?.getString("category") ?: "General Knowledge"
        val xpEarned = arguments?.getInt("xpEarned") ?: 0

        tvFinalScore.text = "Your Score: $score"
        tvCategoryPlayed.text = "Category: $category"
        tvXpEarned.text = "+$xpEarned XP earned!"

        updateXP(xpEarned)
        updateQuestProgress()

        btnRetry.setOnClickListener {
            val bundle = Bundle().apply {
                putString("category", category)
            }
            findNavController().navigate(R.id.gameFragment, bundle)
        }

        btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.playFragment)
        }

        return view
    }

    private fun updateXP(xp: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = firestore.collection("users").document(uid)

        firestore.runTransaction { txn ->
            val snap = txn.get(ref)
            val currentXP = snap.getLong("xp") ?: 0
            txn.update(ref, "xp", currentXP + xp)
        }
    }

    /** Auto-increment daily & weekly quests after finishing a game */
    private fun updateQuestProgress() {
        firestore.collection("quests")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {

                    val type = doc.getString("type") ?: ""
                    val current = (doc.getLong("current") ?: 0).toInt()
                    val goal = (doc.getLong("goal") ?: 1).toInt()

                    val increase = when (type) {
                        "daily" -> 1   // +1 per game
                        "weekly" -> 1  // +1 per game
                        else -> 0
                    }

                    val newCurrent = (current + increase).coerceAtMost(goal)
                    val newProgress = (newCurrent * 100) / goal
                    val completed = newCurrent >= goal

                    doc.reference.update(
                        mapOf(
                            "current" to newCurrent,
                            "progress" to newProgress,
                            "isCompleted" to completed
                        )
                    )
                }
            }
    }
}

package student.projects.quizmaster01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class GameOverFragment : Fragment() {

    private lateinit var tvFinalScore: TextView
    private lateinit var tvCategoryPlayed: TextView
    private lateinit var tvXpEarned: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnBackHome: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game_over, container, false)

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
}

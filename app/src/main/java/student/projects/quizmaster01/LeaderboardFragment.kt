package student.projects.quizmaster01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import student.projects.quizmaster01.R

data class PlayerRank(
    val name: String = "",
    val xp: Int = 0
)

class LeaderboardFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: LeaderboardAdapter
    private val players = mutableListOf<PlayerRank>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        firestore = FirebaseFirestore.getInstance()
        val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvLeaderboard)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = LeaderboardAdapter(players)
        recycler.adapter = adapter

        loadLeaderboard()

        return view
    }

    private fun loadLeaderboard() {
        firestore.collection("users")
            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                players.clear()
                players.addAll(snapshot.documents.mapIndexed { index, doc ->
                    PlayerRank(
                        name = doc.getString("username") ?: "Player ${index + 1}",
                        xp = (doc.getLong("xp") ?: 0).toInt()
                    )
                })
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
    }
}
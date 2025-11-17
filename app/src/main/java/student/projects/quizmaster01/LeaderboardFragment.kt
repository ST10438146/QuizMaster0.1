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

        val mode = "xp"   // change to "dailyXp" or "weeklyXp" later when adding spinner

        firestore.collection("users")
            .orderBy(mode, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                players.clear()

                val list = snapshot.documents.mapIndexed { index, doc ->
                    val xpValue = doc.get("xp")
                    val xpInt = when (xpValue) {
                        is Long -> xpValue.toInt()
                        is Int -> xpValue
                        else -> 0
                    }

                    val rank = index + 1
                    val medal = when (rank) {
                        1 -> "gold"
                        2 -> "silver"
                        3 -> "bronze"
                        else -> ""

                    }

                    PlayerRank(
                        name = doc.getString("username") ?: "Player $rank",
                        xp = (doc.getLong("xp") ?: 0).toInt(),
                        dailyXp = (doc.getLong("dailyXp") ?: 0).toInt(),
                        weeklyXp = (doc.getLong("weeklyXp") ?: 0).toInt(),
                        medals = medal,
                        rank = rank
                    )
                }

                players.addAll(list)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT)
                    .show()
            }
    }

}
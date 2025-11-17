package student.projects.quizmaster01

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import student.projects.quizmaster01.R

class QuestsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: QuestsAdapter
    private val quests = mutableListOf<Quest>()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quests, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvQuests)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = QuestsAdapter(quests) { quest -> claimReward(quest) }
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadQuestsOnce() }

        startRealtimeListener()

        return view
    }

    private fun startRealtimeListener() {
        listenerRegistration?.remove()
        listenerRegistration = firestore.collection("quests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    quests.clear()
                    quests.addAll(snapshot.documents.mapNotNull {
                        Quest(
                            id = it.id,
                            title = it.getString("title") ?: "",
                            description = it.getString("description") ?: "",
                            progress = (it.getLong("progress") ?: 0).toInt(),
                            rewardXP = (it.getLong("rewardXP") ?: 0).toInt(),
                            isCompleted = it.getBoolean("isCompleted") ?: false,
                            type = it.getString("type") ?: "daily",
                            goal = (it.getLong("goal") ?: 0).toInt(),
                            current = (it.getLong("current") ?: 0).toInt()
                        )
                    })
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun loadQuestsOnce() {
        swipeRefresh.isRefreshing = true
        firestore.collection("quests").get()
            .addOnSuccessListener { snapshot ->
                quests.clear()
                quests.addAll(snapshot.documents.mapNotNull {
                    Quest(
                        id = it.id,
                        title = it.getString("title") ?: "",
                        description = it.getString("description") ?: "",
                        progress = (it.getLong("progress") ?: 0).toInt(),
                        rewardXP = (it.getLong("rewardXP") ?: 0).toInt(),
                        isCompleted = it.getBoolean("isCompleted") ?: false
                    )
                })
                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener {
                swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Failed to reload quests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun claimReward(quest: Quest) {
        if (!quest.isCompleted) {
            Toast.makeText(requireContext(), "Complete this quest first!", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)

        firestore.runTransaction { txn ->
            val snapshot = txn.get(userRef)
            val currentXP = snapshot.getLong("xp") ?: 0
            txn.update(userRef, "xp", currentXP + quest.rewardXP)
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "+${quest.rewardXP} XP claimed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}
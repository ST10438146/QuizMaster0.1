package student.projects.quizmaster01

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

data class UserStatus(
    val playerName: String = "Player",
    val xp: Int = 0,
    val coins: Int = 0,
    val activeQuestsCount: Int = 0
)

/**
 * ViewModel for HomeFragment. It manages real-time user status data
 * by listening to changes in Firebase Firestore.
 */
class HomeViewModel : ViewModel() {

    private val _userStatus = MutableLiveData<UserStatus>()
    val userStatus: LiveData<UserStatus> get() = _userStatus

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var statusListener: ListenerRegistration? = null

    init {
        loadUserStatus()
    }

    /** Sets up a real-time listener for current user's data */
    fun loadUserStatus() {
        val uid = auth.currentUser?.uid ?: return

        // Removes any previous listener
        statusListener?.remove()

        statusListener = firestore.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    _userStatus.value = UserStatus("Player", 0, 0, 0)
                    return@addSnapshotListener
                }

                if (doc != null && doc.exists()) {
                    val name = doc.getString("username") ?: "Player"
                    val xp = (doc.getLong("xp") ?: 0).toInt()
                    val coins = (doc.getLong("coins") ?: 0).toInt()
                    val activeQuests = (doc.getLong("activeQuestsCount") ?: 0).toInt()

                    _userStatus.value = UserStatus(name, xp, coins, activeQuests)
                } else {
                    _userStatus.value = UserStatus("Player", 0, 0, 0)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        statusListener?.remove()
    }

    // --- Actions/Navigation Logic ---

    fun onStartSoloGameClicked() {
        viewModelScope.launch {
            // TODO: add business logic like coin deduction or level check
        }
    }

    fun onStartMultiplayerClicked() {
        // TODO: implement matchmaking trigger here
    }
}
package student.projects.quizmaster01

import UserStatus
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
 * ViewModel for HomeFragment. It manages the real-time user status and gamification data
 * by listening to changes in Firebase Firestore (Milestones 1, 4).
 *
 * NOTE: In a production app, Firestore/Auth logic would ideally be encapsulated
 * within a dedicated Repository and injected via Hilt.
 */
class HomeViewModel : ViewModel() {

    // LiveData exposed to the Fragment for observing user status changes
    private val _userStatus = MutableLiveData<UserStatus>()
    val userStatus: LiveData<UserStatus> = _userStatus

    // Firebase dependencies
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Listener for real-time updates
    private var statusListener: ListenerRegistration? = null

    init {
        // Automatically attempt to load user status when the ViewModel is created
        loadUserStatus()
    }

    /**
     * Sets up a real-time listener for the current user's data from Firestore.
     * This method fulfills the 'Realtime: Firestore listeners' requirement from the plan.
     */
    fun loadUserStatus() {
        // Ensure only one listener is active at a time
        statusListener?.remove()

        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            // Handle unauthenticated state (should be rare if AuthActivity works correctly)
            _userStatus.value = UserStatus(playerName = "Please Log In")
            return
        }

        // Path: /artifacts/{appId}/users/{userId}/userProfile/statusDoc
        // Assuming a collection structure for private user data
        val userStatusDocRef = db.collection("artifacts")
            // __app_id is assumed to be available in the environment context
            .document(getAppId())
            .collection("users")
            .document(userId)
            .collection("userProfile")
            .document("statusDoc")

        // Start the real-time listener
        statusListener = userStatusDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Log the error for Firebase Crashlytics
                println("Error listening to user status: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                // Safely convert the document snapshot to the UserStatus data class
                val currentStatus = snapshot.toObject(UserStatus::class.java)
                _userStatus.value = currentStatus ?: UserStatus(userId = userId)
            } else {
                // Document might not exist yet, initialize a default one in memory
                _userStatus.value = UserStatus(userId = userId, playerName = "New Player")
            }
        }
    }

    /**
     * Helper function to retrieve the app ID (simulated for environment context).
     * Replace with actual dependency injection in a full Hilt setup.
     */
    private fun getAppId(): String {
        // NOTE: In a Canvas environment, this would come from the global __app_id variable.
        // We use a placeholder here for compilation purposes.
        return "quizmaster-app-12345"
    }

    /**
     * Cleans up the real-time listener when the ViewModel is destroyed
     * to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        statusListener?.remove()
    }

    // --- Actions/Navigation Logic ---

    /**
     * Called when the user initiates a solo game.
     * In a real app, this would perform logic before navigation (e.g., checking credits).
     */
    fun onStartSoloGameClicked() {
        // Business logic here: e.g., log event, check if player has enough coins.
        viewModelScope.launch {
            // Example: If a check fails, update a StateFlow/LiveData to show a warning dialog.
        }
    }

    /**
     * Called when the user initiates multiplayer matchmaking.
     */
    fun onStartMultiplayerClicked() {
        // Business logic here: e.g., initiate matchmaking request to the backend.
    }
}
package models

/**
 * Data class representing the real-time status of the currently authenticated user.
 * This data is displayed on the HomeFragment (Milestones 1 & 4).
 *
 * @property userId The unique ID of the user (e.g., from Firebase Auth).
 * @property playerName The user's displayed name (e.g., "Msizi Q.L.").
 * @property xp The current experience points.
 * @property coins The current in-game currency/coins.
 * @property activeQuestsCount The number of time-boxed quests the user is currently pursuing.
 */
data class UserStatus(
    val userId: String = "",
    val playerName: String = "Guest Player",
    val xp: Long = 0,
    val coins: Long = 0,
    val activeQuestsCount: Int = 0
)
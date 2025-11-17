package student.projects.quizmaster01

data class PlayerRank(
    val name: String = "",
    val xp: Int = 0,
    val dailyXp: Int = 0,
    val weeklyXp: Int = 0,
    val medals: String = "",  // "gold", "silver", "bronze", or ""
    val rank: Int = 0         // 1, 2, 3, 4...
)

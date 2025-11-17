package student.projects.quizmaster01

data class Quest(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val progress: Int = 0,          // 0 - 100
    val rewardXP: Int = 0,
    val isCompleted: Boolean = false,
    val type: String = "daily",     // daily, weekly, achievement
    val goal: Int = 10,             // e.g. answer 10 questions
    val current: Int = 0            // user's progress count
)

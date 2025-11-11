package student.projects.quizmaster01

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val category: String = ""
)

class GameFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tts: TextToSpeech

    private lateinit var tvQuestion: TextView
    private lateinit var optionButtons: List<Button>
    private lateinit var btnNext: Button
    private lateinit var tvProgress: TextView
    private lateinit var tvScore: TextView

    private var questions: MutableList<QuizQuestion> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var selected = false
    private var ttsReady = false

    // 🔹 Removed SafeArgs line
    // private val args: GameFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        tts = TextToSpeech(requireContext(), this)

        // Bind views
        tvQuestion = view.findViewById(R.id.tvQuestion)
        btnNext = view.findViewById(R.id.btnNext)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvScore = view.findViewById(R.id.tvScore)

        optionButtons = listOf(
            view.findViewById(R.id.btnOption1),
            view.findViewById(R.id.btnOption2),
            view.findViewById(R.id.btnOption3),
            view.findViewById(R.id.btnOption4)
        )

        btnNext.setOnClickListener {
            if (currentIndex < questions.size - 1) {
                currentIndex++
                displayQuestion()
            } else {
                endGame()
            }
        }

        // 🔹 Manual fallback: read the argument passed from PlayFragment
        val category = arguments?.getString("category") ?: "General Knowledge"
        loadQuestions(category)

        return view
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.ENGLISH)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun loadQuestions(category: String) {
        firestore.collection("questions")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snapshot ->
                questions = snapshot.documents.mapNotNull { doc ->
                    QuizQuestion(
                        question = doc.getString("question") ?: "",
                        options = doc.get("options") as? List<String> ?: emptyList(),
                        correctAnswer = doc.getString("correctAnswer") ?: "",
                        category = doc.getString("category") ?: ""
                    )
                }.shuffled().toMutableList()

                if (questions.isEmpty()) {
                    // 🆕 Fallback to local
                    loadLocalQuestions(category)
                } else {
                    currentIndex = 0
                    displayQuestion()
                }
            }
            .addOnFailureListener {
                // 🆕 Fallback to local
                loadLocalQuestions(category)
            }
    }

    /** Local fallback if Firestore not available or empty **/
    private fun loadLocalQuestions(category: String) {
        val localQuestions = listOf(
            // 🧠 General Knowledge (4)
            QuizQuestion("What is the capital city of South Africa?",
                listOf("Johannesburg", "Pretoria", "Cape Town", "Durban"), "Pretoria", "General Knowledge"),
            QuizQuestion("Which ocean lies on the east coast of Africa?",
                listOf("Atlantic", "Indian", "Pacific", "Southern"), "Indian", "General Knowledge"),
            QuizQuestion("How many continents are there on Earth?",
                listOf("5", "6", "7", "8"), "7", "General Knowledge"),
            QuizQuestion("Which is the largest country in the world by area?",
                listOf("USA", "China", "Russia", "Canada"), "Russia", "General Knowledge"),

            // 🔬 Science (4)
            QuizQuestion("What gas do plants absorb from the atmosphere?",
                listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"), "Carbon Dioxide", "Science"),
            QuizQuestion("What planet is known as the Red Planet?",
                listOf("Earth", "Mars", "Venus", "Jupiter"), "Mars", "Science"),
            QuizQuestion("What is the hardest natural substance on Earth?",
                listOf("Gold", "Iron", "Diamond", "Granite"), "Diamond", "Science"),
            QuizQuestion("What part of the cell contains genetic material?",
                listOf("Cytoplasm", "Nucleus", "Cell wall", "Mitochondria"), "Nucleus", "Science"),

            // 🏛️ History (4)
            QuizQuestion("Who was the first president of the United States?",
                listOf("Abraham Lincoln", "Thomas Jefferson", "George Washington", "John Adams"), "George Washington", "History"),
            QuizQuestion("In which year did World War II end?",
                listOf("1940", "1945", "1950", "1939"), "1945", "History"),
            QuizQuestion("Who was Nelson Mandela’s deputy president in 1994?",
                listOf("Thabo Mbeki", "Jacob Zuma", "Cyril Ramaphosa", "Oliver Tambo"), "Thabo Mbeki", "History"),
            QuizQuestion("Which empire built the Colosseum?",
                listOf("Greek Empire", "Roman Empire", "Ottoman Empire", "Persian Empire"), "Roman Empire", "History"),

            // ⚽ Sports (4)
            QuizQuestion("How many players are there in a soccer team?",
                listOf("9", "10", "11", "12"), "11", "Sports"),
            QuizQuestion("What sport is known as 'the gentleman’s game'?",
                listOf("Tennis", "Cricket", "Golf", "Rugby"), "Cricket", "Sports"),
            QuizQuestion("Which country won the 2010 FIFA World Cup?",
                listOf("Brazil", "Germany", "Spain", "Italy"), "Spain", "Sports"),
            QuizQuestion("What is the national sport of Japan?",
                listOf("Karate", "Sumo Wrestling", "Baseball", "Judo"), "Sumo Wrestling", "Sports"),

            // 🎬 Entertainment (4)
            QuizQuestion("Who played Iron Man in the Marvel movies?",
                listOf("Chris Evans", "Robert Downey Jr.", "Chris Hemsworth", "Mark Ruffalo"), "Robert Downey Jr.", "Entertainment"),
            QuizQuestion("Which movie features the song 'Let It Go'?",
                listOf("Moana", "Frozen", "Encanto", "Cinderella"), "Frozen", "Entertainment"),
            QuizQuestion("Who is the author of the Harry Potter series?",
                listOf("J.K. Rowling", "Suzanne Collins", "Rick Riordan", "Stephen King"), "J.K. Rowling", "Entertainment"),
            QuizQuestion("Which band released the album 'Thriller'?",
                listOf("The Beatles", "Queen", "Michael Jackson", "U2"), "Michael Jackson", "Entertainment"),

            // 🧠 Extra Mix (Bonus 5)
            QuizQuestion("What is the largest planet in our solar system?",
                listOf("Earth", "Saturn", "Jupiter", "Neptune"), "Jupiter", "Science"),
            QuizQuestion("Who painted the Mona Lisa?",
                listOf("Van Gogh", "Da Vinci", "Picasso", "Rembrandt"), "Da Vinci", "General Knowledge"),
            QuizQuestion("How many colors are in a rainbow?",
                listOf("5", "6", "7", "8"), "7", "Science"),
            QuizQuestion("In which year did South Africa host the FIFA World Cup?",
                listOf("2006", "2010", "2014", "2018"), "2010", "Sports"),
            QuizQuestion("What movie won the Oscar for Best Picture in 2020?",
                listOf("Joker", "1917", "Parasite", "Ford v Ferrari"), "Parasite", "Entertainment")
        )

        // Filter by category or show general knowledge fallback
        questions = localQuestions.filter {
            it.category.equals(category, ignoreCase = true) || category == "General Knowledge"
        }.shuffled().toMutableList()

        if (questions.isEmpty()) {
            showEmptyState("No local questions found for $category.")
        } else {
            currentIndex = 0
            displayQuestion()
        }
    }

    private fun showEmptyState(message: String) {
        tvQuestion.text = message
        tvProgress.text = ""
        tvScore.text = ""
        btnNext.visibility = View.GONE
        optionButtons.forEach { button ->
            button.text = ""
            button.isEnabled = false
        }
    }

    private fun displayQuestion() {
        if (questions.isEmpty() || currentIndex !in questions.indices) {
            showEmptyState("No more questions available.")
            return
        }

        selected = false
        val q = questions[currentIndex]

        tvQuestion.text = q.question
        tvProgress.text = "Question ${currentIndex + 1}/${questions.size}"
        tvScore.text = "Score: $score"

        for (i in optionButtons.indices) {
            val button = optionButtons[i]
            val text = q.options.getOrNull(i) ?: ""
            button.text = text
            button.setBackgroundColor(Color.parseColor("#EEEEEE"))
            button.isEnabled = true

            button.setOnClickListener {
                if (!selected) checkAnswer(button, q)
            }
        }

        btnNext.visibility = View.INVISIBLE
    }

    private fun checkAnswer(selectedButton: Button, question: QuizQuestion) {
        selected = true
        val correct = selectedButton.text == question.correctAnswer

        if (correct) {
            selectedButton.setBackgroundColor(Color.GREEN)
            score += 10
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            optionButtons.find { it.text == question.correctAnswer }
                ?.setBackgroundColor(Color.GREEN)
        }

        optionButtons.forEach { it.isEnabled = false }
        btnNext.visibility = View.VISIBLE
    }

    private fun endGame() {
        val categoryPlayed = arguments?.getString("category") ?: "General Knowledge"
        val xpEarned = score // each point = 1 XP in your system (you can scale it if you want)

        // Update XP in Firestore
        updateXP(xpEarned)

        // Create bundle for GameOverFragment
        val bundle = Bundle().apply {
            putInt("score", score)
            putString("category", categoryPlayed)
            putInt("xpEarned", xpEarned)
        }

        findNavController().navigate(R.id.gameOverFragment, bundle)
    }

    private fun updateXP(points: Int) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentXP = snapshot.getLong("xp") ?: 0
            transaction.update(userRef, "xp", currentXP + points)
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "+$points XP earned!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (ttsReady) {
            tts.stop()
            tts.shutdown()
        }
    }
}

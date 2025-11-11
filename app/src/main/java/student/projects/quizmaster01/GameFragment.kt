package student.projects.quizmaster01

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

private const val TAG = "GameFragment"

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val category: String = ""
)

class GameFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var tts: TextToSpeech? = null

    private var tvQuestion: TextView? = null
    private var optionButtons: MutableList<Button> = mutableListOf()
    private var btnNext: Button? = null
    private var tvProgress: TextView? = null
    private var tvScore: TextView? = null

    private var questions: MutableList<QuizQuestion> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var selected = false
    private var ttsReady = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView - inflating layout")
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        try {
            firestore = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
        } catch (ex: Exception) {
            Log.e(TAG, "Firebase init error", ex)
            context?.let { Toast.makeText(it, "Firebase init error", Toast.LENGTH_LONG).show() }
        }

        // Safe TTS init
        try {
            tts = TextToSpeech(requireContext(), this)
        } catch (ex: Exception) {
            Log.e(TAG, "TTS init failed", ex)
            tts = null
        }

        // Bind views defensively
        tvQuestion = view.findViewById(R.id.tvQuestion)
        btnNext = view.findViewById(R.id.btnNext)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvScore = view.findViewById(R.id.tvScore)

        // Buttons - collect only non-null buttons
        val b1 = view.findViewById<Button?>(R.id.btnOption1)
        val b2 = view.findViewById<Button?>(R.id.btnOption2)
        val b3 = view.findViewById<Button?>(R.id.btnOption3)
        val b4 = view.findViewById<Button?>(R.id.btnOption4)

        optionButtons.clear()
        listOf(b1, b2, b3, b4).forEachIndexed { idx, b ->
            if (b != null) optionButtons.add(b)
            else Log.e(TAG, "Option button ${idx + 1} is missing in layout (btnOption${idx + 1})")
        }

        // If any essential UI element is missing, show a friendly message and avoid crash
        if (tvQuestion == null || btnNext == null || tvProgress == null || tvScore == null || optionButtons.size < 4) {
            Log.e(TAG, "Essential views missing. tvQuestion: ${tvQuestion != null}, btnNext: ${btnNext != null}, optionButtons: ${optionButtons.size}")
            context?.let {
                Toast.makeText(it, "UI layout incomplete. Check fragment_game.xml IDs.", Toast.LENGTH_LONG).show()
            }
            // Return early to avoid null pointer crashes
            return view
        }

        btnNext?.setOnClickListener {
            try {
                if (currentIndex < questions.size - 1) {
                    currentIndex++
                    displayQuestion()
                } else {
                    endGame()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error in btnNext click", ex)
                context?.let { Toast.makeText(it, "Error advancing question", Toast.LENGTH_SHORT).show() }
            }
        }

        // Read category from arguments safely
        val category = arguments?.getString("category") ?: "General Knowledge"
        Log.d(TAG, "Selected category = $category")

        // Start loading questions (this is async)
        loadQuestions(category)

        return view
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = tts?.setLanguage(Locale.ENGLISH)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                Log.d(TAG, "TTS ready = $ttsReady")
            } catch (ex: Exception) {
                Log.e(TAG, "TTS language set failed", ex)
                ttsReady = false
            }
        } else {
            Log.w(TAG, "TTS init status not SUCCESS: $status")
            ttsReady = false
        }
    }

    private fun loadQuestions(category: String) {
        Log.d(TAG, "loadQuestions for category: $category")
        try {
            firestore.collection("questions")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        questions = snapshot.documents.mapNotNull { doc ->
                            QuizQuestion(
                                question = doc.getString("question") ?: "",
                                options = doc.get("options") as? List<String> ?: emptyList(),
                                correctAnswer = doc.getString("correctAnswer") ?: "",
                                category = doc.getString("category") ?: ""
                            )
                        }.shuffled().toMutableList()

                        Log.d(TAG, "Firestore returned ${questions.size} questions for $category")
                        if (questions.isEmpty()) {
                            loadLocalQuestions(category) // fallback
                        } else {
                            currentIndex = 0
                            displayQuestion()
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error mapping Firestore docs", ex)
                        loadLocalQuestions(category)
                    }
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "Firestore query failed", ex)
                    loadLocalQuestions(category)
                }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception calling Firestore", ex)
            loadLocalQuestions(category)
        }
    }

    /** Local fallback if Firestore not available or empty **/
    private fun loadLocalQuestions(category: String) {
        Log.d(TAG, "Using local fallback questions for $category")
        val localQuestions = listOf(
            // General Knowledge
            QuizQuestion("What is the capital city of South Africa?",
                listOf("Johannesburg", "Pretoria", "Cape Town", "Durban"), "Pretoria", "General Knowledge"),
            QuizQuestion("Which ocean lies on the east coast of Africa?",
                listOf("Atlantic", "Indian", "Pacific", "Southern"), "Indian", "General Knowledge"),
            QuizQuestion("How many continents are there on Earth?",
                listOf("5", "6", "7", "8"), "7", "General Knowledge"),
            QuizQuestion("Which is the largest country in the world by area?",
                listOf("USA", "China", "Russia", "Canada"), "Russia", "General Knowledge"),
            QuizQuestion("Who painted the Mona Lisa?",
                listOf("Van Gogh", "Da Vinci", "Picasso", "Rembrandt"), "Da Vinci", "General Knowledge"),

            // Science
            QuizQuestion("What gas do plants absorb from the atmosphere?",
                listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"), "Carbon Dioxide", "Science"),
            QuizQuestion("What planet is known as the Red Planet?",
                listOf("Earth", "Mars", "Venus", "Jupiter"), "Mars", "Science"),
            QuizQuestion("What is the hardest natural substance on Earth?",
                listOf("Gold", "Iron", "Diamond", "Granite"), "Diamond", "Science"),
            QuizQuestion("What part of the cell contains genetic material?",
                listOf("Cytoplasm", "Nucleus", "Cell wall", "Mitochondria"), "Nucleus", "Science"),
            QuizQuestion("What is the largest planet in our solar system?",
                listOf("Earth", "Saturn", "Jupiter", "Neptune"), "Jupiter", "Science"),

            // History
            QuizQuestion("Who was the first president of the United States?",
                listOf("Abraham Lincoln", "Thomas Jefferson", "George Washington", "John Adams"), "George Washington", "History"),
            QuizQuestion("In which year did World War II end?",
                listOf("1940", "1945", "1950", "1939"), "1945", "History"),
            QuizQuestion("Who was Nelson Mandela’s deputy president in 1994?",
                listOf("Thabo Mbeki", "Jacob Zuma", "Cyril Ramaphosa", "Oliver Tambo"), "Thabo Mbeki", "History"),
            QuizQuestion("Which empire built the Colosseum?",
                listOf("Greek Empire", "Roman Empire", "Ottoman Empire", "Persian Empire"), "Roman Empire", "History"),

            // Sports
            QuizQuestion("How many players are there in a soccer team?",
                listOf("9", "10", "11", "12"), "11", "Sports"),
            QuizQuestion("What sport is known as 'the gentleman’s game'?",
                listOf("Tennis", "Cricket", "Golf", "Rugby"), "Cricket", "Sports"),
            QuizQuestion("Which country won the 2010 FIFA World Cup?",
                listOf("Brazil", "Germany", "Spain", "Italy"), "Spain", "Sports"),
            QuizQuestion("What is the national sport of Japan?",
                listOf("Karate", "Sumo Wrestling", "Baseball", "Judo"), "Sumo Wrestling", "Sports"),
            QuizQuestion("In which year did South Africa host the FIFA World Cup?",
                listOf("2006", "2010", "2014", "2018"), "2010", "Sports"),

            // Entertainment
            QuizQuestion("Who played Iron Man in the Marvel movies?",
                listOf("Chris Evans", "Robert Downey Jr.", "Chris Hemsworth", "Mark Ruffalo"), "Robert Downey Jr.", "Entertainment"),
            QuizQuestion("Which movie features the song 'Let It Go'?",
                listOf("Moana", "Frozen", "Encanto", "Cinderella"), "Frozen", "Entertainment"),
            QuizQuestion("Who is the author of the Harry Potter series?",
                listOf("J.K. Rowling", "Suzanne Collins", "Rick Riordan", "Stephen King"), "J.K. Rowling", "Entertainment"),
            QuizQuestion("Which artist released the album 'Thriller'?",
                listOf("The Beatles", "Queen", "Michael Jackson", "U2"), "Michael Jackson", "Entertainment"),
            QuizQuestion("What movie won the Oscar for Best Picture in 2020?",
                listOf("Joker", "1917", "Parasite", "Ford v Ferrari"), "Parasite", "Entertainment")
        )

        // Filter by category or show general knowledge fallback
        questions = localQuestions.filter {
            it.category.equals(category, ignoreCase = true) || category.equals("General Knowledge", ignoreCase = true)
        }.shuffled().toMutableList()

        Log.d(TAG, "Local fallback produced ${questions.size} questions for $category")

        if (questions.isEmpty()) {
            showEmptyState("No local questions found for $category.")
        } else {
            currentIndex = 0
            displayQuestion()
        }
    }

    private fun showEmptyState(message: String) {
        Log.d(TAG, "showEmptyState: $message")
        tvQuestion?.text = message
        tvProgress?.text = ""
        tvScore?.text = ""
        btnNext?.visibility = View.GONE
        optionButtons.forEach { button ->
            button.text = ""
            button.isEnabled = false
        }
        context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() }
    }

    private fun displayQuestion() {
        if (questions.isEmpty() || currentIndex !in questions.indices) {
            showEmptyState("No more questions available.")
            return
        }

        selected = false
        val q = questions[currentIndex]

        tvQuestion?.text = q.question
        tvProgress?.text = "Question ${currentIndex + 1}/${questions.size}"
        tvScore?.text = "Score: $score"

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

        btnNext?.visibility = View.INVISIBLE

        // speak via TTS safely
        if (ttsReady && tts != null) {
            try {
                val combinedText = buildString {
                    append("Question ${currentIndex + 1}. ${q.question}. ")
                    q.options.forEachIndexed { index, option ->
                        append("Option ${index + 1}: $option. ")
                    }
                }
                tts?.speak(combinedText, TextToSpeech.QUEUE_FLUSH, null, "Q${currentIndex}")
            } catch (ex: Exception) {
                Log.e(TAG, "TTS speak failed", ex)
            }
        }
    }

    private fun checkAnswer(selectedButton: Button, question: QuizQuestion) {
        selected = true
        val correct = selectedButton.text == question.correctAnswer

        if (correct) {
            selectedButton.setBackgroundColor(Color.GREEN)
            score += 10
            context?.let { if (ttsReady) tts?.speak("Correct", TextToSpeech.QUEUE_FLUSH, null, "ANS") }
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            optionButtons.find { it.text == question.correctAnswer }
                ?.setBackgroundColor(Color.GREEN)
            context?.let { if (ttsReady) tts?.speak("Wrong. The correct answer is ${question.correctAnswer}", TextToSpeech.QUEUE_FLUSH, null, "ANS") }
        }

        optionButtons.forEach { it.isEnabled = false }
        btnNext?.visibility = View.VISIBLE
    }

    private fun endGame() {
        Log.d(TAG, "endGame - score=$score")
        val categoryPlayed = arguments?.getString("category") ?: "General Knowledge"
        val xpEarned = score

        updateXP(xpEarned)

        val bundle = Bundle().apply {
            putInt("score", score)
            putString("category", categoryPlayed)
            putInt("xpEarned", xpEarned)
        }

        // Navigate to GameOverFragment safely
        try {
            findNavController().navigate(R.id.gameOverFragment, bundle)
        } catch (ex: Exception) {
            Log.e(TAG, "Navigation to GameOverFragment failed", ex)
            context?.let { Toast.makeText(it, "Could not open summary screen", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun updateXP(points: Int) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No logged-in user; skipping XP update")
            return
        }
        val userRef = firestore.collection("users").document(uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentXP = snapshot.getLong("xp") ?: 0L
                transaction.update(userRef, "xp", currentXP + points)
            }.addOnSuccessListener {
                context?.let { Toast.makeText(it, "+$points XP earned!", Toast.LENGTH_SHORT).show() }
            }.addOnFailureListener { ex ->
                Log.e(TAG, "XP update failed", ex)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception while updating XP", ex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            tts?.let {
                it.stop()
                it.shutdown()
            }
        } catch (ex: Exception) {
            Log.w(TAG, "Error shutting down TTS", ex)
        }
    }
}

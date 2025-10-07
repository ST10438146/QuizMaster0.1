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
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import student.projects.quizmaster01.R
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
    private lateinit var tts: TextToSpeech // 🔊 TTS engine

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        tts = TextToSpeech(requireContext(), this) // 🔊 Initialize TTS

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

        return view
    }

    // 🔊 Called when TextToSpeech is ready
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.ENGLISH)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    /** Load questions for this category **/
    private fun loadQuestions(category: String) {
        firestore.collection("questions")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    loadLocalQuestions(category)
                } else {
                    questions = snapshot.documents.mapNotNull { doc ->
                        QuizQuestion(
                            question = doc.getString("question") ?: "",
                            options = doc.get("options") as? List<String> ?: emptyList(),
                            correctAnswer = doc.getString("correctAnswer") ?: "",
                            category = doc.getString("category") ?: ""
                        )
                    }.shuffled().toMutableList()
                    displayQuestion()
                }
            }
            .addOnFailureListener {
                loadLocalQuestions(category)
            }
    }

    /** Fallback if Firestore not available **/
    private fun loadLocalQuestions(category: String) {
        val sample = listOf(
            QuizQuestion(
                "What planet is known as the Red Planet?",
                listOf("Earth", "Mars", "Venus", "Jupiter"),
                "Mars",
                "Science"
            ),
            QuizQuestion(
                "Who painted the Mona Lisa?",
                listOf("Van Gogh", "Da Vinci", "Picasso", "Rembrandt"),
                "Da Vinci",
                "General Knowledge"
            ),
            QuizQuestion(
                "How many players in a soccer team?",
                listOf("9", "10", "11", "12"),
                "11",
                "Sports"
            )
        )
        questions = sample.filter { it.category == category || category == "General Knowledge" }.toMutableList()
        displayQuestion()
    }

    /** Show current question **/
    private fun displayQuestion() {
        selected = false
        val q = questions[currentIndex]

        tvQuestion.text = q.question
        tvProgress.text = "Question ${currentIndex + 1}/${questions.size}"
        tvScore.text = "Score: $score"

        // 🔊 Accessibility description for the question
        tvQuestion.contentDescription = "Question: ${q.question}"

        for (i in optionButtons.indices) {
            val button = optionButtons[i]
            val text = q.options.getOrNull(i) ?: ""
            button.text = text
            button.setBackgroundColor(Color.parseColor("#EEEEEE"))
            button.isEnabled = true

            // 🔊 Add speakable text (TalkBack and TTS)
            button.contentDescription = "Option ${i + 1}: $text"
            button.setOnLongClickListener {
                if (ttsReady) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                true
            }

            button.setOnClickListener {
                if (!selected) checkAnswer(button, q)
            }
        }

        // 🔊 Auto-speak question + options if TTS ready
        if (ttsReady) {
            val combinedText = buildString {
                append("Question ${currentIndex + 1}. ${q.question}. ")
                q.options.forEachIndexed { index, option ->
                    append("Option ${index + 1}: $option. ")
                }
            }
            tts.speak(combinedText, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        btnNext.visibility = View.INVISIBLE
    }

    /** Handle answer selection **/
    private fun checkAnswer(selectedButton: Button, question: QuizQuestion) {
        selected = true
        val correct = selectedButton.text == question.correctAnswer

        if (correct) {
            selectedButton.setBackgroundColor(Color.GREEN)
            score += 10
            if (ttsReady) tts.speak("Correct!", TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            optionButtons.find { it.text == question.correctAnswer }
                ?.setBackgroundColor(Color.GREEN)
            if (ttsReady) tts.speak("Wrong. The correct answer is ${question.correctAnswer}", TextToSpeech.QUEUE_FLUSH, null, null)
        }

        optionButtons.forEach { it.isEnabled = false }
        btnNext.visibility = View.VISIBLE
    }

    private fun endGame() {
        Toast.makeText(requireContext(), "Game Over! Score: $score", Toast.LENGTH_LONG).show()
        if (ttsReady) tts.speak("Game Over! Your final score is $score", TextToSpeech.QUEUE_FLUSH, null, null)
        updateXP(score)
        findNavController().popBackStack()
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
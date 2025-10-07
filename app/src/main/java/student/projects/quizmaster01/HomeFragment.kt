package student.projects.quizmaster01

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUserStatus()
        viewModel.userStatus.observe(viewLifecycleOwner) { status ->
            view.findViewById<TextView>(R.id.tvPlayerName)?.text = status.playerName
            view.findViewById<TextView>(R.id.tvPlayerScore)?.text = "${String.format("%,d", status.xp)} XP"
            view.findViewById<TextView>(R.id.tvCoinsCount)?.text = String.format("%,d", status.coins)
            view.findViewById<TextView>(R.id.tvActiveQuests)?.text = "${status.activeQuestsCount} Active Quests"
        }

        view.findViewById<View>(R.id.btn_start_solo)?.setOnClickListener {
            viewModel.onStartSoloGameClicked()
            // TODO: replace with a real destination when CategorySelectFragment exists
            // findNavController().navigate(R.id.categorySelectFragment)
        }

        view.findViewById<View>(R.id.btn_start_multiplayer)?.setOnClickListener {
            viewModel.onStartMultiplayerClicked()
            // TODO: create matchmakingFragment before navigating
        }

        view.findViewById<View>(R.id.season_pass_card)?.setOnClickListener {
            findNavController().navigate(R.id.nav_quests)
        }
    }
}
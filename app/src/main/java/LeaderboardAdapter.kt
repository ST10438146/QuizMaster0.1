package student.projects.quizmaster01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import student.projects.quizmaster01.R

class LeaderboardAdapter(
    private val players: List<PlayerRank>
) : RecyclerView.Adapter<LeaderboardAdapter.PlayerViewHolder>() {

    inner class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.tvRank)
        val avatar: ImageView = view.findViewById(R.id.ivAvatar)
        val name: TextView = view.findViewById(R.id.tvPlayerName)
        val xp: TextView = view.findViewById(R.id.tvXP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.rank.text = "#${position + 1}"
        holder.name.text = player.name
        holder.xp.text = "XP: ${player.xp}"
    }

    override fun getItemCount() = players.size
}
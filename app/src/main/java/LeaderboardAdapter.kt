package student.projects.quizmaster01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import student.projects.quizmaster01.R

class LeaderboardAdapter(private val players: List<PlayerRank>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank = itemView.findViewById<TextView>(R.id.tvRank)
        val ivMedal = itemView.findViewById<ImageView>(R.id.ivMedal)
        val tvPlayerName = itemView.findViewById<TextView>(R.id.tvPlayerName)
        val tvPlayerScore = itemView.findViewById<TextView>(R.id.tvPlayerScore)
        val ivAvatar = itemView.findViewById<ImageView>(R.id.ivAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = players.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]

        holder.tvRank.text = "#${player.rank}"
        holder.tvPlayerName.text = player.name
        holder.tvPlayerScore.text = "${player.xp} XP"

        // Show medal for top 3
        when (player.medals) {
            "gold" -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal_gold)
            }
            "silver" -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal_silver)
            }
            "bronze" -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.ivMedal.setImageResource(R.drawable.ic_medal_bronze)
            }
            else -> holder.ivMedal.visibility = View.GONE
        }
    }
}

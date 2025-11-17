package student.projects.quizmaster01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import student.projects.quizmaster01.R

class QuestsAdapter(
    private val quests: List<Quest>,
    private val onClaimClicked: (Quest) -> Unit
) : RecyclerView.Adapter<QuestsAdapter.QuestViewHolder>() {

    inner class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.tvQuestType)
        val title: TextView = view.findViewById(R.id.tvQuestTitle)
        val desc: TextView = view.findViewById(R.id.tvQuestDesc)
        val progressLabel: TextView = view.findViewById(R.id.tvQuestProgress)
        val progress: ProgressBar = view.findViewById(R.id.progressQuest)
        val claim: Button = view.findViewById(R.id.btnClaimReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        holder.type.text = quest.type.uppercase()
        holder.title.text = quest.title
        holder.desc.text = quest.description
        holder.progress.progress = quest.progress
        holder.progressLabel.text = "${quest.current}/${quest.goal}"

        if (quest.isCompleted) {
            holder.claim.visibility = View.VISIBLE
            holder.claim.text = "CLAIM +${quest.rewardXP} XP"
        } else {
            holder.claim.visibility = View.GONE
        }

        holder.claim.setOnClickListener {
            // Button animation
            holder.claim.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(120)
                .withEndAction {
                    holder.claim.animate().scaleX(1f).scaleY(1f)
                }
            onClaimClicked(quest)
        }
    }

    override fun getItemCount() = quests.size
}

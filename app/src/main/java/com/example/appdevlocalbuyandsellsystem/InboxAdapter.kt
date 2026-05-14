package com.example.appdevlocalbuyandsellsystem

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

/**
 * Adapter for the Messages (Inbox) list.
 */
class InboxAdapter(
    private val inboxList: List<InboxMessage>,
    private val onItemClick: (InboxMessage) -> Unit,
    private val onItemLongClick: (InboxMessage) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvInboxName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvInboxLastMessage)
        val tvDate: TextView = itemView.findViewById(R.id.tvInboxDate)
        val tvUnread: TextView = itemView.findViewById(R.id.tvInboxUnread)
        val ivProfile: ImageView = itemView.findViewById(R.id.ivInboxProfile)
        val container: View = itemView.findViewById(R.id.inboxItemContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val conversation = inboxList[position]
        holder.tvName.text = conversation.name
        holder.tvLastMessage.text = conversation.lastMessage
        holder.tvDate.text = conversation.time

        // Load Profile Image
        if (conversation.otherUserId.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(conversation.otherUserId).get()
                .addOnSuccessListener { doc ->
                    val profileImg = doc.getString("profileImageUrl")
                    if (!profileImg.isNullOrEmpty()) {
                        val context = holder.itemView.context
                        val file = if (profileImg.contains("/")) File(profileImg) else File(context.filesDir, profileImg)
                        Glide.with(context)
                            .load(file)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user)
                            .into(holder.ivProfile)
                    } else {
                        holder.ivProfile.setImageResource(R.drawable.ic_user)
                    }
                }
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_user)
        }

        // Selection background logic
        if (conversation.isSelected) {
            holder.container.setBackgroundColor(Color.parseColor("#D1E7E4")) // Light teal selection
        } else {
            holder.container.setBackgroundColor(Color.TRANSPARENT)
        }

        if (conversation.unreadCount > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = conversation.unreadCount.toString()
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(conversation) }
        
        holder.itemView.setOnLongClickListener {
            onItemLongClick(conversation)
            true
        }
    }

    override fun getItemCount(): Int = inboxList.size
}

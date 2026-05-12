package com.example.appdevlocalbuyandsellsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the Messages (Inbox) list.
 */
class InboxAdapter(
    private val inboxList: List<InboxMessage>,
    private val onItemClick: (InboxMessage) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvInboxName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvInboxLastMessage)
        val tvDate: TextView = itemView.findViewById(R.id.tvInboxDate)
        val tvUnread: TextView = itemView.findViewById(R.id.tvInboxUnread)
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

        if (conversation.unreadCount > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = conversation.unreadCount.toString()
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(conversation) }
    }

    override fun getItemCount(): Int = inboxList.size
}
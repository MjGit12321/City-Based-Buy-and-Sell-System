package com.example.appdevlocalbuyandsellsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter to handle multiple chat item types (Sent, Received, Timestamp).
 */
class ChatAdapter(private val chatList: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
        private const val TYPE_TIMESTAMP = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (chatList[position].type) {
            MessageType.SENT -> TYPE_SENT
            MessageType.RECEIVED -> TYPE_RECEIVED
            MessageType.TIMESTAMP -> TYPE_TIMESTAMP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_sent, parent, false)
                SentViewHolder(view)
            }
            TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_received, parent, false)
                ReceivedViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_timestamp, parent, false)
                TimestampViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatList[position]
        when (holder) {
            is SentViewHolder -> {
                holder.tvMessage.text = message.content
                holder.tvTime.text = message.time
            }
            is ReceivedViewHolder -> {
                holder.tvMessage.text = message.content
                holder.tvTime.text = message.time
            }
            is TimestampViewHolder -> holder.tvTime.text = message.time
        }
    }

    override fun getItemCount(): Int = chatList.size

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    class TimestampViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTimestamp)
    }
}

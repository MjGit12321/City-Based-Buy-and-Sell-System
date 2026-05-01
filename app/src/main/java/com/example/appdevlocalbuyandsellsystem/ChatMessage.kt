package com.example.appdevlocalbuyandsellsystem

/**
 * Data model for a chat message.
 */
data class ChatMessage(
    val content: String,
    val time: String,
    val type: MessageType
)

enum class MessageType {
    SENT,
    RECEIVED,
    TIMESTAMP
}
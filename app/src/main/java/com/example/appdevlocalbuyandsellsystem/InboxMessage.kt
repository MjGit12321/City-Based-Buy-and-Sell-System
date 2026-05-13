package com.example.appdevlocalbuyandsellsystem

/**
 * Data model for a conversation in the Inbox.
 */
data class InboxMessage(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    var originalDocId: String = "",
    var otherUserId: String = "",
    var isSelected: Boolean = false
)

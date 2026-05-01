package com.example.appdevlocalbuyandsellsystem

/**
 * Data model for a conversation in the Inbox.
 */
data class InboxMessage(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val date: String,
    val unreadCount: Int = 0
)
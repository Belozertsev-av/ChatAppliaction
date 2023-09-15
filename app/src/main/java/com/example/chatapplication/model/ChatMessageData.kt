package com.example.chatapplication.model

data class ChatMessageData (
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long
) {
    constructor() : this ("", "", "", "", -1)
}
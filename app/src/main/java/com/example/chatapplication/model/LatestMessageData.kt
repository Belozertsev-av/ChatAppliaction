package com.example.chatapplication.model

data class LatestMessageData(val user: User, val text: String, val date: Long) {
    constructor() : this(User(), "", -1)
}
package com.example.chatapplication.model

class User(val uid: String, val username: String, val profileImage: String) {
    constructor() : this("", "", "")
}
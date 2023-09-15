package com.example.chatapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImage: String): Parcelable {
    constructor() : this("", "", "")
}
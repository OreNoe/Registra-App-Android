package com.example.marcelo.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val lvl: Int = 0,
    val encargado: String = "",
):Parcelable
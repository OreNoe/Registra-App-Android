package com.example.marcelo.entities


data class User (
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val lvl: Int = 0,
    val encargado: String = "",
    val active: Boolean = true
    )
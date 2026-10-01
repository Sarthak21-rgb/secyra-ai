package com.secyra.ai.domain.model

data class User(
    val id: String,
    val email: String,
    val isPro: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

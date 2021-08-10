package com.title.joke.dto

data class RefreshTokenDto(
    val refresh_token: String,
    val access_token: String,
    val expires_at: Long
)
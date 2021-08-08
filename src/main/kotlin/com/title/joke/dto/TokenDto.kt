package com.title.joke.dto

data class TokenDto(
    val refresh_token: String,
    val access_token: String,
    val expires_at: String,
    val athlete: AthleteDto
)

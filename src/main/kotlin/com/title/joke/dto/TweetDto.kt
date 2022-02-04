package com.title.joke.dto

data class TweetDto(
    val id: String,
    val text: String
)

data class TwitterResponseDto(
    val data: List<TweetDto>,
    val meta: MetaDto
)

data class MetaDto(
    val result_count: Int,
    val next_token: String?
)

package com.title.joke.dto

data class EventDataDto(
    val object_type: String,
    val object_id: String,
    val aspect_type: String,
    val updates: Map<String, String>,
    val owner_id: String,
    val subscription_id: String,
    val event_time: String
)

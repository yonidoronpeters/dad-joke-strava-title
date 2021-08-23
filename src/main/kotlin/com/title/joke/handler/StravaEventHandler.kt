package com.title.joke.handler

import com.title.joke.dto.EventDataDto

interface StravaEventHandler {
    fun isApplicable(event: EventDataDto): Boolean
    fun handle(event: EventDataDto)
}
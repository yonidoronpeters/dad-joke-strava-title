package com.title.joke.service

import com.title.joke.dto.EventDataDto
import com.title.joke.handler.StravaEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventsService(private val eventsHandlers: List<StravaEventHandler>) {
    private val logger = LoggerFactory.getLogger(EventsService::class.java)

    suspend fun updateActivity(event: EventDataDto) {
        logger.debug("Event handlers: $eventsHandlers")
        eventsHandlers
            .filter { it.isApplicable(event) }
            .map { it.handle(event) }
    }
}
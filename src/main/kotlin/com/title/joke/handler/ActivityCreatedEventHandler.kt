package com.title.joke.handler

import com.title.joke.dto.EventDataDto
import com.title.joke.service.StravaTitleService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ActivityCreatedEventHandler(
    private val titleService: StravaTitleService
) : StravaEventHandler {
    private val logger = LoggerFactory.getLogger(ActivityCreatedEventHandler::class.java)

    private val maxEntries = 30
    private val eventsCache = object : LinkedHashMap<String, EventDataDto>(maxEntries) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, EventDataDto>?): Boolean {
            return size > maxEntries
        }
    }

    override fun isApplicable(event: EventDataDto): Boolean =
        event.object_type == "activity" && event.aspect_type == "create"

    override fun handle(event: EventDataDto) {
        if (!isDuplicateCreateEvent(event)) titleService.updateTitle(event)
    }

    private fun isDuplicateCreateEvent(event: EventDataDto): Boolean {
        if (eventsCache.containsKey(event.object_id)) {
            logger.info("Received duplicate event $event")
            return true
        } else {
            eventsCache[event.object_id] = event
        }
        return false
    }
}
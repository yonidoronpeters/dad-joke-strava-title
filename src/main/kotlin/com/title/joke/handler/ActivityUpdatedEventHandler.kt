package com.title.joke.handler

import com.title.joke.dto.EventDataDto
import com.title.joke.service.StravaTitleService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ActivityUpdatedEventHandler(
    private val titleService: StravaTitleService
) : StravaEventHandler {
    private val logger = LoggerFactory.getLogger(ActivityUpdatedEventHandler::class.java)

    override fun isApplicable(event: EventDataDto): Boolean =
        event.object_type == "activity" && event.aspect_type == "update"

    override fun handle(event: EventDataDto) {
        if (event.updates.containsKey("title") &&
            (event.updates["title"]?.trim().equals("next title", ignoreCase = true) ||
                    event.updates["title"]?.trim().equals("n", ignoreCase = true))
        ) {
            logger.info("User requested different title for activity: ${event.object_id}")
            titleService.updateTitle(event)
        }
    }
}
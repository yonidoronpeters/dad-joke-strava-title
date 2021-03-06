package com.title.joke.controller

import com.title.joke.dto.EventDataDto
import com.title.joke.service.EventsService
import kotlinx.coroutines.GlobalScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlinx.coroutines.launch

@RestController
@RequestMapping("subscribe")
class EventsController(
    @Value("\${strava.subscription-id}")
    val subscriptionId: String,
    private val eventsService: EventsService
) {
    private val logger = LoggerFactory.getLogger(EventsController::class.java)

    @PostMapping("callback", consumes = ["application/json"])
    fun callback(@RequestBody eventData: EventDataDto): ResponseEntity<String> {
        logger.info("Received event: $eventData")
        if (subscriptionId == eventData.subscription_id) {
            GlobalScope.launch {
                eventsService.updateActivity(eventData)
            }
            return ResponseEntity.ok().build()
        }
        logger.warn("Unauthorized event received: $eventData")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
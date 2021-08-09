package com.title.joke.controller

import com.title.joke.service.ActivityService
import com.title.joke.dto.EventDataDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("subscribe")
class EventsController(private val activityService: ActivityService) {
    private val logger = LoggerFactory.getLogger(EventsController::class.java)

    @PostMapping("callback", consumes = ["application/json"])
    fun callback(@RequestBody eventData: EventDataDto): ResponseEntity<String> {
        logger.info("Received event: $eventData")
        activityService.updateActivity(eventData)
        return ResponseEntity.ok().build()
    }
}
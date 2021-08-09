package com.title.joke.controller

import com.title.joke.service.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("subscribe")
class SubscriptionController(
    private val service: SubscriptionService,
    @Value("\${joke.verify-token}")
    val verifyToken: String
) {
    private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)

    @PostMapping
    fun createSubscription(): String {
        // Only 1 subscription can be created for the APP. All events will be sent for athletes that authorize the app
        // This endpoint should be called manually 1 time after the app is deployed. Then app will receive events for all
        // athletes to url joke.callback-url
        val subscriptionId = service.createSubscription()
        return "Subscription created successfully with id: $subscriptionId"
    }

    @GetMapping("callback", produces = ["application/json"])
    fun callback(
        @RequestParam("hub.mode") mode: String,
        @RequestParam("hub.challenge") challenge: String,
        @RequestParam("hub.verify_token") sentToken: String
    ): ResponseEntity<String> {
        logger.info("Received subscription creation challenge")

        if (verifyToken == sentToken) {
            logger.info("Subscription challenge matches :)")
            return ResponseEntity.ok().body(
                """
                {
                    "hub.challenge": "$challenge" 
                }
            """.trimIndent()
            )
        }
        logger.error("Wrong subscription challenge returned. Expected '$verifyToken', but received '$sentToken'")
        throw RuntimeException("Returned challenge is wrong")
    }

    // TODO
    // add endpoint to eventscontroller
    // view subscription
    // delete subscription
}
package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.SubscriptionDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    @Value("\${strava.client.id}")
    val clientId: String,
    @Value("\${strava.client.secret}")
    val clientSecret: String,
    @Value("\${joke.callback-url}")
    val callbackUrl: String,
    @Value("\${joke.verify-token}")
    val verifyToken: String,
    @Value("\${strava.push-subscription-url}")
    val subscriptionUrl: String
) {
    private val logger = LoggerFactory.getLogger(SubscriptionService::class.java)

    fun createSubscription(): String {
        logger.info("Creating Strava event subscription for app")

        FuelManager.instance.baseParams = listOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "callback_url" to callbackUrl,
            "verify_token" to verifyToken
        )
        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, _, result) = subscriptionUrl
            .httpPost()
            .responseObject<SubscriptionDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Error when creating push subscription from Strava", e)
                throw e
            }
            is Result.Success -> {
                val dto: SubscriptionDto = result.get()
                logger.info("Subscription created successfully with id: ${dto.id}")
                return dto.id
            }
            else -> throw RuntimeException("Unknown error while trying to subscribe to push events")
        }
    }
}
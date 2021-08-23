package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.ActivityDto
import com.title.joke.dto.EventDataDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.unbescape.json.JsonEscape

@Service
class StravaTitleService(
    @Value("\${strava.base-activity-url}")
    val baseActivityUrl: String,
    private val tokenService: OAuthTokenService,
    private val titleService: TitleService
) {
    private val logger = LoggerFactory.getLogger(StravaTitleService::class.java)

    fun updateTitle(event: EventDataDto) {
        logger.info("Setting title for activity: ${event.object_id}")

        val bearerToken = tokenService.getTokenForAthlete(event.owner_id)
        val activityTitle = titleService.generateTitle()
        updateTitleOnStrava(bearerToken, activityTitle, event)
    }

    private fun updateTitleOnStrava(bearerToken: String, activityTitle: String, event: EventDataDto) {
        logger.info("Updating activity title for $baseActivityUrl${event.object_id}")

        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, response, result) = "$baseActivityUrl${event.object_id}"
            .httpPut()
            .header("Authorization", bearerToken)
            .jsonBody(
                """
                         {
                            "name": "${JsonEscape.escapeJson(activityTitle)}",
                            "description": "Get fun dad jokes for your activity at https://www.titleworkout.pro"
                         }
                     """.trimIndent()
            )
            .responseObject<ActivityDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Strava API returned ${response.statusCode} with body ${response.body()}")
                logger.error("Error when updating activity with id: ${event.object_id}", e)
                throw e
            }
            is Result.Success -> {
                val dto: ActivityDto = result.get()
                logger.info("Successfully updated activity with id:${dto.id} to title: ${dto.name}")
            }
        }
    }
}
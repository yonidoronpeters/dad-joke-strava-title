package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.jackson.objectBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.ActivityDto
import com.title.joke.dto.EventDataDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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
        val existingDescription = getDescription(mapper, bearerToken, event.object_id)
        val newDescription = generateDescription(existingDescription)
        val activity = ActivityDto(name = activityTitle, description = newDescription, id = event.object_id)

        val (_, response, result) = "$baseActivityUrl${event.object_id}"
            .httpPut()
            .header("Authorization", bearerToken)
            .objectBody(bodyObject = activity, mapper = mapper)
            .responseObject<ActivityDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Strava API returned ${response.statusCode} with body ${response.body().asString("application/json")}")
                logger.error("Error when updating activity with id: ${event.object_id}", e)
                throw e
            }
            is Result.Success -> {
                val dto: ActivityDto = result.get()
                logger.info("Successfully updated activity with id:${dto.id} to title: ${dto.name}")
            }
        }
    }

    private fun generateDescription(existingDescription: String?): String {
        if (existingDescription?.isNotBlank() == true) {
            return if (existingDescription.contains(DESCRIPTION)) {
                existingDescription
            } else {
                "$existingDescription\n\n$DESCRIPTION"
            }
        }
        return DESCRIPTION
    }

    private fun getDescription(mapper: ObjectMapper, bearerToken: String, activityId: String): String? {
        val (_, response, result) = "$baseActivityUrl$activityId"
            .httpGet()
            .header("Authorization", bearerToken)
            .responseObject<ActivityDto>(mapper)
        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Strava API returned ${response.statusCode} with body ${response.body().asString("application/json")}")
                logger.error("Error when getting exiting activity with id: $activityId", e)
                throw e
            }
            is Result.Success -> {
                val dto: ActivityDto = result.get()
                logger.debug("Successfully fetched activity with id:${dto.id} to title: ${dto.name}")
                return result.get().description
            }
        }
    }

    companion object {
        private const val DESCRIPTION = "Title by https://www.titleworkout.pro"
    }
}
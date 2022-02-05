package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.RefreshTokenDto
import com.title.joke.dto.TokenDto
import com.title.joke.entity.AthleteToken
import com.title.joke.repository.AthleteTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class OAuthTokenService(
    @Value("\${strava.client.id}")
    val clientId: String,
    @Value("\${strava.client.secret}")
    val clientSecret: String,
    @Value("\${strava.auth.token-url}")
    val stravaOAuthTokenUrl: String,
    private val repository: AthleteTokenRepository
) {
    private val logger = LoggerFactory.getLogger(OAuthTokenService::class.java)

    fun authorizeApp(authorizationCode: String) {
        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, _, result) = stravaOAuthTokenUrl
            .httpPost(
                listOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "code" to authorizationCode,
                    "grant_type" to "authorization_code"
                )
            )
            .responseObject<TokenDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Error while getting token from Strava", e)
                throw e
            }
            is Result.Success -> {
                val dto = result.get()
                logger.info(dto.toString())
                this.saveToken(dto)
            }
            else -> throw RuntimeException("Unknown error while trying to get token from Strava")
        }
    }

    private fun saveToken(dto: TokenDto) {
        val athleteToken = AthleteToken(
            dto.athlete.id,
            dto.athlete.firstname,
            dto.athlete.lastname,
            dto.access_token,
            dto.refresh_token,
            dto.expires_at
        )
        logger.info("Saving athlete token to database for athlete: ${athleteToken.id}")
        repository.save(athleteToken)
    }

    fun getTokenForAthlete(athleteId: String): String {
        val athleteEntity = repository.getOne(athleteId)
        if (Date().before(Date.from(Instant.ofEpochSecond(athleteEntity.expires_at)))) {
            return "Bearer ${athleteEntity.access_token}"
        }
        return refreshToken(athleteEntity)
    }

    private fun refreshToken(athleteEntity: AthleteToken): String {
        logger.debug("Refreshing token for athlete: ${athleteEntity.id}")
        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, response, result) = stravaOAuthTokenUrl
            .httpPost(
                listOf(
                    "client_id" to clientId,
                    "client_secret" to clientSecret,
                    "grant_type" to "refresh_token",
                    "refresh_token" to athleteEntity.refresh_token
                )
            )
            .responseObject<RefreshTokenDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Strava API returned ${response.statusCode} with body ${response.body()}")
                logger.error("Error while trying to refresh access token from Strava", e)
                throw e
            }
            is Result.Success -> {
                val dto = result.get()
                logger.info(dto.toString())
                val refreshedAthleteToken = AthleteToken(
                    athleteEntity.copy(
                        access_token = dto.access_token,
                        refresh_token = dto.refresh_token,
                        expires_at = dto.expires_at
                    )
                )
                repository.save(refreshedAthleteToken)
                return "Bearer ${refreshedAthleteToken.access_token}"
            }
        }
    }

    fun deactivateAthleteToken(athleteId: String) {
        val athleteToken = repository.findOne(athleteId)
        repository.save(AthleteToken(athleteToken.copy(is_active = false)))
    }
}
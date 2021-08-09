package com.title.joke.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.JokeDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DadJokeTitleService(
    @Value("\${joke.dad-joke-url}")
    val dadJokeUrl: String
) : TitleService {
    private val logger = LoggerFactory.getLogger(DadJokeTitleService::class.java)

    override fun generateTitle(): String {
        logger.info("Getting dad joke from $dadJokeUrl")

        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val (_, _, result) = dadJokeUrl
            .httpGet()
            .appendHeader("Accept", "application/json")
            .appendHeader("User-Agent", "Dad joke Strava title (https://github.com/yonidoronpeters/dad-joke-strava-title)")
            .responseObject<JokeDto>(mapper)

        when (result) {
            is Result.Failure -> {
                val e = result.getException()
                logger.error("Error when fetching joke from $dadJokeUrl", e)
                throw e
            }
            is Result.Success -> {
                val dto: JokeDto = result.get()
                logger.info("Fetched the joke: \"${dto.joke}\"")
                return dto.joke
            }
        }
    }
}
package com.title.joke.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import com.title.joke.dto.TwitterResponseDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TwitterDadJokeService(
    @Value("\${twitter.api-url}")
    val twitterApiUrl: String,
    @Value("\${twitter.token}")
    val twitterToken: String,
    private val mapper: ObjectMapper
) : TitleService {
    private val logger = LoggerFactory.getLogger(TwitterDadJokeService::class.java)
    private val maxResults = 50

    override fun generateTitle(): String {
        logger.info("Getting dad joke from Twitter")

        var pageCounter = 0
        var paginationToken: String? = null
        val pageNumber = (0..10).random()
        val tweetNumber = (0..49).random()
        logger.info("Twitter paging info: pageNumber: $pageNumber, tweetNumber: $tweetNumber")
        var dto: TwitterResponseDto
        do {
            val (_, response, result) = "$twitterApiUrl/users/905028905026846720/tweets"
                .httpGet(listOfNotNull("max_results" to maxResults, "pagination_token" to paginationToken))
                .appendHeader("Authorization", "Bearer $twitterToken")
                .responseObject<TwitterResponseDto>(mapper)
            when (result) {
                is Result.Failure -> {
                    val e = result.getException()
                    logger.error("Twitter API returned ${response.statusCode} with body ${response.body().asString("application/json")}")
                    logger.error("Error when fetching data joke from Twitter", e)
                    throw e
                }
                is Result.Success -> {
                    dto = result.get()
                    paginationToken = dto.meta.next_token
                    logger.debug("Next pagination token: $paginationToken")
                }
            }
        } while (pageCounter++ < pageNumber)
        val joke = dto.data[tweetNumber].text
        return if (joke.startsWith("RT ") || joke.contains("https?://|@".toRegex())) generateTitle() else joke.replace("\n\n", "\n")
    }
}
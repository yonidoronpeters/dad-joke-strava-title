package com.title.joke.controller

import com.title.joke.service.OAuthTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("oauth")
class OAuthController(
    val service: OAuthTokenService
) {

    private val logger: Logger = LoggerFactory.getLogger(OAuthController::class.java)

    @GetMapping("/exchange_token")
    fun authorizeApp(
        @RequestParam("code") code: String,
        @RequestParam("scope") scopes: List<String>
    ): String {
        logger.info("user permitted scopes: $scopes")
        if (!scopes.contains("activity:write")) {
            return "App doesn't work without write permissions. Please try again and check the box for 'Upload your activities from Dad joke title generator to Strava'."
        }
        service.authorizeApp(code)
        return "authorization successful"
    }
}
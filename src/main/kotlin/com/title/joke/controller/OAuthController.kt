package com.title.joke.controller

import com.title.joke.service.OAuthTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@Controller
@RequestMapping("oauth")
class OAuthController(
    private val service: OAuthTokenService
) {
    private val logger: Logger = LoggerFactory.getLogger(OAuthController::class.java)

    @GetMapping("/exchange_token")
    fun authorizeApp(
        @RequestParam("code") code: String,
        @RequestParam("scope") scopes: List<String>
    ): String {
        logger.info("user permitted scopes: $scopes")
        if (!scopes.containsAll(listOf("activity:write", "activity:read_all"))) {
            return "error"
        }
        service.authorizeApp(code)
        return "success"
    }
}
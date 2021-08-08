package com.title.joke.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("register")
class RegistrationController(
    @Value("\${strava.auth.authorize-url}")
    val redirectUrl: String
) {
    private val logger = LoggerFactory.getLogger(RegistrationController::class.java)

    @GetMapping
    fun redirectToStrava(response: HttpServletResponse) {
        logger.info("Redirect url is: $redirectUrl")
        return response.sendRedirect(redirectUrl)
    }
}
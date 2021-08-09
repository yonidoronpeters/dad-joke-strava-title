package com.title.joke.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletResponse

@Controller
class RegistrationController(
    @Value("\${strava.auth.authorize-url}")
    val redirectUrl: String
) {
    private val logger = LoggerFactory.getLogger(RegistrationController::class.java)

    @GetMapping("register")
    fun redirectToStrava(response: HttpServletResponse) {
        logger.info("Redirect url is: $redirectUrl")
        return response.sendRedirect(redirectUrl)
    }

    @RequestMapping("/")
    internal fun index(): String {
        return "index"
    }
}
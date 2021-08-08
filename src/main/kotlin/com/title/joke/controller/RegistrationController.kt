package com.title.joke.controller

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

    @GetMapping
    fun redirectToStrava(response: HttpServletResponse) {
        return response.sendRedirect(redirectUrl)
    }
}
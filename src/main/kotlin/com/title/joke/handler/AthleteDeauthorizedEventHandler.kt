package com.title.joke.handler

import com.title.joke.dto.EventDataDto
import com.title.joke.service.OAuthTokenService
import org.springframework.stereotype.Component

@Component
class AthleteDeauthorizedEventHandler(
    private val tokenService: OAuthTokenService
) : StravaEventHandler {
    override fun isApplicable(event: EventDataDto): Boolean =
        event.object_type == "athlete" && event.aspect_type == "update" && event.updates["authorized"] == "false"

    override fun handle(event: EventDataDto) {
        tokenService.deactivateAthleteToken(event.owner_id)
    }
}
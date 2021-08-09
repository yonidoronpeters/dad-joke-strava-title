package com.title.joke.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class AthleteToken(
    @Id
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val access_token: String = "",
    val refresh_token: String = "",
    val expires_at: Long = 0
)

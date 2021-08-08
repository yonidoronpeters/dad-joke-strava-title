package com.title.joke.repository

import com.title.joke.entity.AthleteToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AthleteTokenRepository: JpaRepository<AthleteToken, String>
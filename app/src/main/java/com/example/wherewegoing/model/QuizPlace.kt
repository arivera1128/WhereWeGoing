package com.example.wherewegoing.model

data class QuizPlace(
    val id: String,
    val name: String,
    val description: String,
    val traits: Set<String>
)

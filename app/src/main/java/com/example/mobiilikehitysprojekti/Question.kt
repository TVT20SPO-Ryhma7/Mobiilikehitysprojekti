package com.example.mobiilikehitysprojekti

data class Question(
    val question: String = "",
    val imageURL: String? = "",
    val optionOne: String = "",
    val optionTwo: String = "",
    val optionThree: String = "",
    val optionFour: String = "",
    val correctOption: Int = 0
)

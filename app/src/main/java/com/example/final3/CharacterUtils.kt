package com.example.final3

object CharacterUtils {
    fun getDrawableByCharacterId(id: Int): Int = when (id) {
        1 -> R.drawable.ch1 // ربانزل
        2 -> R.drawable.ch2 // موآنا
        3 -> R.drawable.ch3 // اّنــــا
        4 -> R.drawable.ch4 // كريستوف
        5 -> R.drawable.ch5 // هيرو
        6 -> R.drawable.ch6 // علاء الدين
        else -> R.drawable.ch1
    }

    fun getNameByCharacterId(id: Int): String = when (id) {
        1 -> "ربانزل"
        2 -> "موانا"
        3 -> "اّنا"
        4 -> "كريستوف"
        5 -> "هيرو"
        6 -> "علاء الدين"
        else -> "شخصية افتراضية"
    }
}

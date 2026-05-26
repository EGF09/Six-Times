package com.example.a6times.utils

object Constants {
    const val FIREBASE_DATABASE_URL = "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app"
    const val MAX_WORD_PROGRESS = 6
    
    // Time constants in milliseconds
    const val ONE_DAY_MS = 86400000L
    const val ONE_WEEK_MS = 7L * ONE_DAY_MS
    const val ONE_MONTH_MS = 30L * ONE_DAY_MS
    const val THREE_MONTHS_MS = 90L * ONE_DAY_MS
    const val SIX_MONTHS_MS = 180L * ONE_DAY_MS
    const val ONE_YEAR_MS = 365L * ONE_DAY_MS

    // SharedPreferences Keys
    const val PREFS_USER = "UserPrefs"
    const val PREFS_KEY_USER_NAME = "UserName"
    const val PREFS_APP = "AppPrefs"
    const val PREFS_KEY_LAST_LOGIN = "lastLoginDay"
    const val PREFS_KEY_STREAK = "currentStreak"
    const val PREFS_SETTINGS = "AppSettings"
    const val PREFS_KEY_EXAM_LIMIT = "ExamQuestionLimit"
    const val PREFS_WORD_CHAIN = "WordChainPrefs"
    const val PREFS_KEY_LAST_IMAGE = "lastImageUrl"
    const val PREFS_KEY_LAST_STORY = "lastStoryText"

    // Wordle Constants
    const val WORDLE_ANIMATION_DURATION = 300L
    const val WORDLE_ANIMATION_DELAY_MULTIPLIER = 200L
    const val WORDLE_END_DELAY = 600L
}

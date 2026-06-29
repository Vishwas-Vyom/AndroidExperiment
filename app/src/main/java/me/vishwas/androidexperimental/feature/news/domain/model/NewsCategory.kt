package me.vishwas.androidexperimental.feature.news.domain.model

enum class NewsCategory(val value: String, val displayName: String) {
    GENERAL("general", "For You"),
    BUSINESS("business", "Business"),
    TECHNOLOGY("technology", "Tech"),
    SPORTS("sports", "Sports"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    HEALTH("health", "Health"),
    SCIENCE("science", "Science"),
}

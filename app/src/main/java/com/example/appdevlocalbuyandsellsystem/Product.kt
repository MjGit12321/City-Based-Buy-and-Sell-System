package com.example.appdevlocalbuyandsellsystem

/**
 * Data model for a Product.
 * Holds information about price, name, and description.
 */
data class Product(
    val price: String,
    val name: String,
    val description: String,
    val username: String = "User123",
    val rating: Float = 0.0f,
    var isFavorite: Boolean = false
)
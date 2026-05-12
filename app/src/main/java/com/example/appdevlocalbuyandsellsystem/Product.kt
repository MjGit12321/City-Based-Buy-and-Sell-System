package com.example.appdevlocalbuyandsellsystem

import java.io.Serializable
import java.util.Date

/**
 * Data model for a Product.
 * Holds information about price, name, and description.
 */
data class Product(
    val price: String = "",
    val name: String = "",
    val description: String = "",
    val username: String = "",
    val sellerName: String = "",
    val sellerID: String = "",
    val rating: Float = 0.0f,
    val baranggay: String = "",
    val city: String = "",
    val province: String = "",
    val region: String = "",
    var isFavorite: Boolean = false,
    val timestamp: Date? = null,
    val location : String = baranggay + city + province + region
) : Serializable
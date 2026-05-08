package com.example.appdevlocalbuyandsellsystem

import com.google.firebase.Timestamp
import java.io.Serializable

/**
 * Data model for a Product.
 * Holds information about price, name, and description.
 */
data class Product(
    val price: String = "",
    val name: String = "",
    val description: String = "",
    val username: String = "",
    val sellerID: String = "",
    val rating: Float = 0.0f,
    val baranggay: String = "",
    val city: String = "",
    val province: String = "",
    var isFavorite: Boolean = false,
    val timestamp: Timestamp? = null
) : Serializable
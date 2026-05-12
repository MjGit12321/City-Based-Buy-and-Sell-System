package com.example.appdevlocalbuyandsellsystem

import java.io.Serializable
import java.util.Date

/**
 * Data model for a Product.
 * Uses default values to ensure a no-arg constructor for Firestore.
 */
data class Product(
    var price: String = "",
    var name: String = "",
    var description: String = "",
    var username: String = "",
    var sellerName: String = "",
    var sellerID: String = "", // Primary ID field
    var sellerId: String = "", // Fallback ID field for different database casings
    var rating: Float = 0.0f,
    var baranggay: String = "",
    var city: String = "",
    var province: String = "",
    var region: String = "",
    var isFavorite: Boolean = false,
    var timestamp: Date? = null,
    var location: String = ""
) : Serializable {
    
    // Helper to get the correct seller ID regardless of field name in database
    fun getSafeSellerId(): String {
        return if (sellerID.isNotEmpty()) sellerID else sellerId
    }
}

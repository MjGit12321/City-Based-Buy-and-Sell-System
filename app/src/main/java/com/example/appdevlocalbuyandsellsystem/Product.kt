package com.example.appdevlocalbuyandsellsystem

import java.io.Serializable
import java.util.Date

/**
 * Data model for a Product.
 * Simplified for stable Firestore serialization.
 */
data class Product(
    var price: String = "",
    var name: String = "",
    var description: String = "",
    var username: String = "",
    var sellerName: String = "",
    var sellerID: String = "", 
    var sellerId: String = "", // Fallback
    var rating: Float = 0.0f,
    var baranggay: String = "",
    var city: String = "",
    var province: String = "",
    var region: String = "",
    var isFavorite: Boolean = false,
    var timestamp: Date? = null,
    var location: String = "",
    var documentId: String = ""
) : Serializable {
    fun getSafeSellerId(): String {
        return if (sellerID.isNotEmpty()) sellerID else sellerId
    }
}

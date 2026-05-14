package com.example.appdevlocalbuyandsellsystem

import java.io.Serializable
import java.util.Date

/**
 * Data model for a Product.
 * Simplified to avoid Firestore mapping conflicts.
 */
data class Product(
    var price: String = "",
    var name: String = "",
    var description: String = "",
    var username: String = "",
    var sellerName: String = "",
    var sellerId: String = "",
    var rating: Float = 0.0f,
    var baranggay: String = "",
    var city: String = "",
    var province: String = "",
    var region: String = "",
    var isFavorite: Boolean = false,
    var timestamp: Date? = null,
    var location: String = "",
    var documentId: String = "",
    var imageUrl: String = ""
) : Serializable {
    fun getSafeSellerId(): String = sellerId
}

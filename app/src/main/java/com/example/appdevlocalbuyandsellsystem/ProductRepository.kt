package com.example.appdevlocalbuyandsellsystem

/**
 * A shared repository that holds products fetched from Firebase.
 */
object ProductRepository {
    // Keep the list, but make it empty.
    // We will fill this dynamically from Firestore later.
    val products = mutableListOf<Product>()

    fun getFavorites(): MutableList<Product> {
        return products.filter { it.isFavorite }.toMutableList()
    }
}

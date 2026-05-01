package com.example.appdevlocalbuyandsellsystem

/**
 * A simple shared repository to manage products across different screens.
 */
object ProductRepository {
    val products = mutableListOf(
        Product("₱1,299", "StreetFlex Comfort Sneakers", "Lightweight and stylish sneakers designed for everyday wear. Made with breathable materials to keep your feet cool and comfortable all day.", "Janahn", 5.0f),
        Product("₱1,500", "Vintage Camera", "Fully functional classic film camera.", "RetroCollector", 4.6f),
        Product("₱25,000", "Mountain Bike", "High-performance bike for all terrains.", "TrailBlazer", 4.8f),
        Product("₱3,200", "Wireless Headphones", "Noise-cancelling with long battery life.", "AudioPhil", 4.5f),
        Product("₱850", "Designer Mug", "Hand-crafted ceramic mug with unique art.", "ArtisanCorner", 4.9f),
        Product("₱12,000", "Smartphone", "Slightly used Android phone with great camera.", "TechGeek", 4.3f),
        Product("₱4,500", "Modern Lamp", "Minimalist design for your workspace.", "HomeDecor", 4.7f),
        Product("₱7,200", "Smart Watch", "Track your health and notifications.", "WearTech", 4.4f),
        Product("₱1,200", "Yoga Mat", "Non-slip surface for comfortable workouts.", "ZenFitness", 4.6f),
        Product("₱35,000", "Gaming Laptop", "Powerful specs for smooth gaming.", "LevelUpStore", 4.8f),
        Product("₱2,800", "Bluetooth Speaker", "Portable sound with deep bass.", "VibeAudio", 4.2f),
        Product("₱950", "Coffee Grinder", "Manual stainless steel coffee bean grinder.", "BrewLover", 4.7f),
        Product("₱1,800", "Mechanical Keyboard", "RGB backlit with tactile blue switches.", "ClickyTyper", 4.5f),
        Product("₱5,500", "Leather Backpack", "Genuine leather with multiple compartments.", "TravelLite", 4.9f),
        Product("₱2,100", "Electric Kettle", "Fast boiling with auto shut-off feature.", "KitchenEssential", 4.4f)
    )

    fun getFavorites(): MutableList<Product> {
        return products.filter { it.isFavorite }.toMutableList()
    }
}
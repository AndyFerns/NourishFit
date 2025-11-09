package com.example.nourishfit.data.db

data class PresetFood(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

// --- Hardcoded List of Food Macroes ---
// TODO: Utilise web APIs and access a larger dataset of foods
val presetFoodList = listOf(
    // Fruits
    PresetFood("Apple", 95, 0, 25, 0),
    PresetFood("Banana", 105, 1, 27, 0),
    PresetFood("Orange", 62, 1, 15, 0),
    PresetFood("Strawberries (1 cup)", 49, 1, 12, 0),
    PresetFood("Blueberries (1 cup)", 84, 1, 21, 1),
    PresetFood("Avocado (half)", 160, 2, 9, 15),

    // Vegetables
    PresetFood("Broccoli (1 cup)", 55, 4, 11, 1),
    PresetFood("Spinach (1 cup, raw)", 7, 1, 1, 0),
    PresetFood("Carrot (medium)", 25, 1, 6, 0),
    PresetFood("Bell Pepper (red)", 31, 1, 6, 0),
    PresetFood("Sweet Potato (medium)", 103, 2, 24, 0),
    PresetFood("Cucumber (half)", 20, 1, 4, 0),

    // Proteins
    PresetFood("Chicken Breast (100g)", 165, 31, 0, 4),
    PresetFood("Salmon (100g)", 208, 20, 0, 13),
    PresetFood("Tuna (can, in water)", 116, 26, 0, 1),
    PresetFood("Egg (large)", 78, 6, 1, 5),
    PresetFood("Tofu (100g)", 76, 8, 3, 5),
    PresetFood("Ground Beef (90% lean, 100g)", 199, 28, 0, 9),

    // Grains & Carbs
    PresetFood("Brown Rice (1 cup cooked)", 216, 5, 45, 2),
    PresetFood("White Rice (1 cup cooked)", 204, 4, 45, 0),
    PresetFood("Quinoa (1 cup cooked)", 222, 8, 39, 4),
    PresetFood("Whole Wheat Bread (1 slice)", 81, 4, 14, 1),
    PresetFood("White Bread (1 slice)", 79, 3, 15, 1),
    PresetFood("Oats (1/2 cup, dry)", 150, 5, 27, 3),
    PresetFood("Pasta (1 cup cooked)", 220, 8, 43, 1),

    // Dairy & Alternatives
    PresetFood("Milk (1 cup, 2%)", 122, 8, 12, 5),
    PresetFood("Greek Yogurt (1 cup)", 100, 17, 6, 0),
    PresetFood("Cheddar Cheese (28g)", 114, 7, 0, 9),
    PresetFood("Almond Milk (1 cup)", 39, 1, 3, 3),

    // Nuts & Fats
    PresetFood("Almonds (28g)", 164, 6, 6, 14),
    PresetFood("Peanut Butter (2 tbsp)", 191, 7, 8, 16),
    PresetFood("Olive Oil (1 tbsp)", 119, 0, 0, 14),

    // Misc
    PresetFood("Protein Shake (1 scoop)", 120, 24, 3, 1),
    PresetFood("Dark Chocolate (30g)", 170, 2, 13, 12)
)
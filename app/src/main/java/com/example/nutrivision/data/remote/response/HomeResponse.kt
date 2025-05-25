package com.example.nutrivision.data.remote.response

import com.google.gson.annotations.SerializedName

data class HomeResponse(

	@field:SerializedName("meal_logs")
	val mealLogs: MealLogs? = null,

	@field:SerializedName("user")
	val user: User? = null
)

data class Snacks(

	@field:SerializedName("total_calories")
	val totalCalories: Int? = null,

	@field:SerializedName("meals")
	val meals: List<MealsItem?>? = null
)

data class Breakfast(

	@field:SerializedName("total_calories")
	val totalCalories: Int? = null,

	@field:SerializedName("meals")
	val meals: List<MealsItem?>? = null
)

data class MealLogs(

	@field:SerializedName("Breakfast")
	val breakfast: Breakfast? = null,

	@field:SerializedName("Dinner")
	val dinner: Dinner? = null,

	@field:SerializedName("Snacks")
	val snacks: Snacks? = null,

	@field:SerializedName("Lunch")
	val lunch: Lunch? = null
)

data class User(

	@field:SerializedName("calories_eaten")
	val caloriesEaten: Int? = null,

	@field:SerializedName("calories_per_day")
	val caloriesPerDay: Int? = null,

	@field:SerializedName("calories_left")
	val caloriesLeft: Int? = null,

	@field:SerializedName("carbohydrates_eaten")
	val carbohydratesEaten: Float? = null,

	@field:SerializedName("fats_eaten")
	val fatsEaten: Float? = null,

	@field:SerializedName("carbohydrates_per_day")
	val carbohydratesPerDay: Float? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("proteins_eaten")
	val proteinsEaten: Float? = null,

	@field:SerializedName("proteins_per_day")
	val proteinsPerDay: Float? = null,

	@field:SerializedName("fats_per_day")
	val fatsPerDay: Float? = null
)

data class MealsItem(

	@field:SerializedName("food_name")
	val foodName: String? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("food_id")
	val foodId: Int? = null,

	@field:SerializedName("meal_id")
	val mealId: Int? = null
)

data class Lunch(

	@field:SerializedName("total_calories")
	val totalCalories: Int? = null,

	@field:SerializedName("meals")
	val meals: List<MealsItem?>? = null
)

data class Dinner(

	@field:SerializedName("total_calories")
	val totalCalories: Int? = null,

	@field:SerializedName("meals")
	val meals: List<MealsItem?>? = null
)

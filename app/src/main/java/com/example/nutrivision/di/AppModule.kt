package com.example.nutrivision.di

import android.content.Context
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.TokenManager
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.api.AuthService
import com.example.nutrivision.data.remote.api.ChatService
import com.example.nutrivision.data.remote.api.CommentService
import com.example.nutrivision.data.remote.api.FoodLogService
import com.example.nutrivision.data.remote.api.FoodService
import com.example.nutrivision.data.remote.api.RecipeService
import com.example.nutrivision.data.remote.api.UserService
import com.example.nutrivision.data.remote.network.ApiConfig
import com.example.nutrivision.data.repository.AuthRepository
import com.example.nutrivision.data.repository.ChatRepository
import com.example.nutrivision.data.repository.CommentRepository
import com.example.nutrivision.data.repository.FoodLogRepository
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.RecipeRepository
import com.example.nutrivision.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingPreferences(
        @ApplicationContext context: Context
    ): SettingPreferences {
        return SettingPreferences.getInstance(context.dataStore)
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        pref: SettingPreferences
    ): TokenManager = TokenManager(pref)

    @Provides
    @Singleton
    fun provideAuthService(
        pref: SettingPreferences
    ): AuthService {
        return ApiConfig.createService(pref, AuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authService: AuthService
    ): AuthRepository {
        return AuthRepository(authService)
    }

    @Provides
    @Singleton
    fun provideUserService(
        pref: SettingPreferences
    ): UserService {
        return ApiConfig.createService(pref, UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userService: UserService
    ): UserRepository {
        return UserRepository(userService)
    }

    @Provides
    @Singleton
    fun provideRecipeService(
        pref: SettingPreferences
    ): RecipeService {
        return ApiConfig.createService(pref, RecipeService::class.java)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeService: RecipeService
    ): RecipeRepository {
        return RecipeRepository(recipeService)
    }

    @Provides
    @Singleton
    fun provideCommentService(
        pref: SettingPreferences
    ): CommentService {
        return ApiConfig.createService(pref, CommentService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        commentService: CommentService
    ): CommentRepository {
        return CommentRepository(commentService)
    }

    @Provides
    @Singleton
    fun provideFoodService(
        pref: SettingPreferences
    ): FoodService {
        return ApiConfig.createService(pref, FoodService::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(
        foodService: FoodService
    ): FoodRepository {
        return FoodRepository(foodService)
    }

    @Provides
    @Singleton
    fun provideFoodLogService(
        pref: SettingPreferences
    ): FoodLogService {
        return ApiConfig.createService(pref, FoodLogService::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodLogRepository(
        foodLogService: FoodLogService
    ): FoodLogRepository {
        return FoodLogRepository(foodLogService)
    }

    @Provides
    @Singleton
    fun provideChatService(
        pref: SettingPreferences
    ): ChatService {
        return ApiConfig.createService(pref, ChatService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatService: ChatService
    ): ChatRepository {
        return ChatRepository(chatService)
    }

    @Provides
    @Singleton
    fun provideMarkwon(
        @ApplicationContext context: Context
    ): Markwon {
        return Markwon.create(context)
    }
}
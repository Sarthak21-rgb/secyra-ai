package com.secyra.ai.core.di

import android.content.Context
// import com.secyra.ai.core.network.RevenueCatConfig
// import com.secyra.ai.core.network.SupabaseConfig
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.core.security.AIProvider
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.telecom.CallNotificationManager
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import com.secyra.ai.core.telecom.PhoneAccountRegistrar
import com.secyra.ai.data.repository.CallLogRepository
import com.secyra.ai.data.repository.ContactRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSecurityEngine(): SecurityEngine {
        return SecurityEngine()
    }

    @Provides
    @Singleton
    fun provideAIProvider(): AIProvider {
        return AIProvider()
    }

    // TODO: Uncomment when Supabase dependencies are properly configured
    // @Provides
    // @Singleton
    // fun provideSupabaseConfig(): SupabaseConfig {
    //     return SupabaseConfig()
    // }

    // TODO: Uncomment when RevenueCat dependencies are properly configured
    // @Provides
    // @Singleton
    // fun provideRevenueCatConfig(): RevenueCatConfig {
    //     return RevenueCatConfig()
    // }

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideCallLogRepository(
        @ApplicationContext context: Context,
        permissionManager: PermissionManager
    ): CallLogRepository {
        return CallLogRepository(context, permissionManager)
    }

    @Provides
    @Singleton
    fun provideContactRepository(
        @ApplicationContext context: Context,
        permissionManager: PermissionManager
    ): ContactRepository {
        return ContactRepository(context, permissionManager)
    }

    @Provides
    @Singleton
    fun provideCallNotificationManager(
        @ApplicationContext context: Context
    ): CallNotificationManager {
        return CallNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideCallManager(
        @ApplicationContext context: Context,
        notificationManager: CallNotificationManager
    ): CallManager {
        return CallManager(context, notificationManager)
    }

    @Provides
    @Singleton
    fun provideDefaultPhoneRoleManager(
        @ApplicationContext context: Context
    ): DefaultPhoneRoleManager {
        return DefaultPhoneRoleManager(context)
    }

    @Provides
    @Singleton
    fun providePhoneAccountRegistrar(
        @ApplicationContext context: Context
    ): PhoneAccountRegistrar {
        return PhoneAccountRegistrar(context)
    }
}

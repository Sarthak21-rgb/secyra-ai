package com.secyra.ai.core.di

import android.content.Context
import com.secyra.ai.core.network.SupabaseConfig
import com.secyra.ai.core.notifications.CallNotificationManager
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.core.security.AIProvider
import com.secyra.ai.core.security.LlamaProvider
import com.secyra.ai.core.threat.PlaceholderThreatProvider
import com.secyra.ai.core.threat.ThreatIntelligenceProvider
import com.secyra.ai.core.threat.ThreatIntelligenceService
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.core.security.SpamDetectionEngine
import com.secyra.ai.core.security.CallerIdentificationService
import com.secyra.ai.core.security.AdvancedSpamDetectionEngine
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.telecom.CallScreeningRoleManager
import com.secyra.ai.core.telecom.CallGroupingService
import com.secyra.ai.core.telecom.CallGroupingConfig
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import com.secyra.ai.core.telecom.PhoneAccountRegistrar
import com.secyra.ai.core.telecom.SmsRoleManager
import com.secyra.ai.data.repository.BlocklistManager
import com.secyra.ai.data.repository.CallLogRepository
import com.secyra.ai.data.repository.ContactRepository
import com.secyra.ai.data.repository.EmailRepository
import com.secyra.ai.data.repository.SmsRepository
import com.secyra.ai.feature.messages.SmsSecurityAnalyzer
import com.secyra.ai.feature.search.SearchViewModel
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
    fun provideCallNotificationManager(
        @ApplicationContext context: Context
    ): CallNotificationManager {
        return CallNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideLlamaProvider(
        supabaseConfig: SupabaseConfig
    ): LlamaProvider {
        return LlamaProvider(supabaseConfig)
    }

    @Provides
    @Singleton
    fun provideThreatIntelligenceProvider(
        supabaseConfig: SupabaseConfig
    ): ThreatIntelligenceProvider {
        return PlaceholderThreatProvider(supabaseConfig)
    }

    @Provides
    @Singleton
    fun provideThreatIntelligenceService(
        provider: ThreatIntelligenceProvider
    ): ThreatIntelligenceService {
        return ThreatIntelligenceService(provider)
    }

    @Provides
    @Singleton
    fun provideAIProvider(
        llamaProvider: LlamaProvider
    ): AIProvider {
        return AIProvider(llamaProvider)
    }

    @Provides
    @Singleton
    fun provideSupabaseConfig(): SupabaseConfig {
        return SupabaseConfig()
    }

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
        permissionManager: PermissionManager,
        callerIdentificationService: CallerIdentificationService,
        advancedSpamDetectionEngine: AdvancedSpamDetectionEngine
    ): CallLogRepository {
        return CallLogRepository(context, permissionManager, callerIdentificationService, advancedSpamDetectionEngine)
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
    fun provideCallManager(
        @ApplicationContext context: Context
    ): CallManager {
        return CallManager(context)
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

    @Provides
    @Singleton
    fun provideSmsRepository(
        @ApplicationContext context: Context
    ): SmsRepository {
        return SmsRepository(context)
    }

    @Provides
    @Singleton
    fun provideEmailRepository(
        @ApplicationContext context: Context,
        permissionManager: PermissionManager
    ): EmailRepository {
        return EmailRepository(context, permissionManager)
    }

    @Provides
    @Singleton
    fun provideSmsRoleManager(
        @ApplicationContext context: Context
    ): SmsRoleManager {
        return SmsRoleManager(context)
    }

    @Provides
    @Singleton
    fun provideSmsSecurityAnalyzer(
        securityEngine: SecurityEngine
    ): SmsSecurityAnalyzer {
        return SmsSecurityAnalyzer(securityEngine)
    }

    @Provides
    @Singleton
    fun provideCallScreeningRoleManager(
        @ApplicationContext context: Context
    ): CallScreeningRoleManager {
        return CallScreeningRoleManager(context)
    }

    @Provides
    @Singleton
    fun provideBlocklistManager(
        @ApplicationContext context: Context
    ): BlocklistManager {
        return BlocklistManager(context)
    }

    @Provides
    @Singleton
    fun provideCallerIdentificationService(
        @ApplicationContext context: Context
    ): CallerIdentificationService {
        return CallerIdentificationService(context)
    }

    @Provides
    @Singleton
    fun provideAdvancedSpamDetectionEngine(
        @ApplicationContext context: Context,
        callerIdentificationService: CallerIdentificationService
    ): AdvancedSpamDetectionEngine {
        return AdvancedSpamDetectionEngine(context, callerIdentificationService)
    }

    @Provides
    @Singleton
    fun provideSpamDetectionEngine(
        @ApplicationContext context: Context,
        blocklistManager: BlocklistManager
    ): SpamDetectionEngine {
        val engine = SpamDetectionEngine(context)
        engine.setBlocklistManager(blocklistManager)
        return engine
    }

    @Provides
    @Singleton
    fun provideCallGroupingConfig(): CallGroupingConfig {
        return CallGroupingConfig(groupingWindowHours = 23)
    }

    @Provides
    @Singleton
    fun provideCallGroupingService(
        config: CallGroupingConfig
    ): CallGroupingService {
        return CallGroupingService(config)
    }
}

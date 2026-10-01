package com.secyra.ai.core.network

// import io.github.jan.supabase.SupabaseClient
// import io.github.jan.supabase.createSupabaseClient
// import io.github.jan.supabase.auth.auth
// import io.github.jan.supabase.postgrest.postgrest
// import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase configuration for SECYRA AI backend.
 * 
 * IMPORTANT: 
 * - Use only the ANON/public key in the Android app
 * - NEVER use service-role keys in the app
 * - Configure Row Level Security (RLS) in Supabase
 * - A user must never be able to access another user's private data
 */
@Singleton
class SupabaseConfig @Inject constructor() {

    // TODO: Replace with actual Supabase project credentials
    // These should be configured in build.gradle.kts with BuildConfig
    // or loaded from secure configuration
    private val supabaseUrl = "https://your-project.supabase.co"
    private val supabaseKey = "your-anon-key"

    /**
     * Create and configure Supabase client
     */
    // TODO: Uncomment when Supabase dependencies are properly configured
    // fun createClient(): SupabaseClient {
    //     return createSupabaseClient(
    //         supabaseUrl = supabaseUrl,
    //         supabaseKey = supabaseKey
    //     ) {
    //         install(auth)
    //         install(postgrest)
    //         install(storage)
    //     }
    // }

    companion object {
        /**
         * Database table names
         */
        const val TABLE_USERS = "users"
        const val TABLE_SECURITY_EVENTS = "security_events"
        const val TABLE_AI_CONVERSATIONS = "ai_conversations"
        const val TABLE_SUBSCRIPTIONS = "subscriptions"
        const val TABLE_PRIVACY_SETTINGS = "privacy_settings"

        /**
         * Storage bucket names
         */
        const val BUCKET_SCREENSHOTS = "screenshots"
        const val BUCKET_ANALYSIS_FILES = "analysis_files"
    }
}

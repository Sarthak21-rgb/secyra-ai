package com.secyra.ai.core.network

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

    // Supabase project credentials
    val supabaseUrl: String = "https://qzzaqzddxendznpyivot.supabase.co/"
    val supabaseKey: String = "sb_publishable_6tVfqgas0qqaHZYQ69kAZw_uULBr3kr"

    /**
     * Edge Function endpoints
     */
    val edgeFunctionChat: String = "$supabaseUrl/functions/v1/ai-chat"
    val edgeFunctionAnalyze: String = "$supabaseUrl/functions/v1/ai-analyze"

    companion object {
        /**
         * Database table names
         */
        const val TABLE_USERS = "users"
        const val TABLE_SECURITY_EVENTS = "security_events"
        const val TABLE_AI_CONVERSATIONS = "ai_conversations"
        const val TABLE_SUBSCRIPTIONS = "subscriptions"
        const val TABLE_PRIVACY_SETTINGS = "privacy_settings"
        const val TABLE_USAGE_LOGS = "usage_logs"

        /**
         * Storage bucket names
         */
        const val BUCKET_SCREENSHOTS = "screenshots"
        const val BUCKET_ANALYSIS_FILES = "analysis_files"
    }
}

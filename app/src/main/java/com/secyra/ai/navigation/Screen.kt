package com.secyra.ai.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calls : Screen("calls")
    object Scan : Screen("scan")
    object AI : Screen("ai")
    object Security : Screen("security")
    object Privacy : Screen("privacy")
    object Settings : Screen("settings")
    object Learn : Screen("learn")
    object LinkSecurity : Screen("link_security")
    object MessageSecurity : Screen("message_security")
    object ProSubscription : Screen("pro_subscription")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object AllActions : Screen("all_actions")
    object ActivityHistory : Screen("activity_history")
    object Search : Screen("search")
}

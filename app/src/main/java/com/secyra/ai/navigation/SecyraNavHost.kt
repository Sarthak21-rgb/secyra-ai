package com.secyra.ai.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.feature.ai.AIScreen
import com.secyra.ai.feature.calls.CallsScreen
import com.secyra.ai.feature.home.HomeScreen
import com.secyra.ai.feature.learn.LearnScreen
import com.secyra.ai.feature.link.LinkSecurityScreen
import com.secyra.ai.feature.message.MessageSecurityScreen
import com.secyra.ai.feature.onboarding.DefaultDialerOnboardingScreen
import com.secyra.ai.feature.privacy.PrivacyScreen
import com.secyra.ai.feature.scan.ScanScreen
import com.secyra.ai.feature.search.SearchScreen
import com.secyra.ai.feature.security.SecurityScreen
import com.secyra.ai.feature.settings.SettingsScreen
import com.secyra.ai.feature.subscription.ProSubscriptionScreen
import com.secyra.ai.navigation.components.SecyraBottomNavigation

@Composable
fun SecyraNavHost() {
    val navController = rememberNavController()
    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.Calls,
        Screen.Scan,
        Screen.AI,
        Screen.Security
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry = navController.currentBackStackEntryAsState().value
            val currentDestination = navBackStackEntry?.destination
            if (currentDestination?.route in bottomNavScreens.map { it.route }) {
                SecyraBottomNavigation(
                    currentDestination = currentDestination,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            composable(Screen.Calls.route) {
                val callsViewModel = hiltViewModel<com.secyra.ai.feature.calls.CallsViewModel>()
                CallsScreen(
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.DefaultDialerOnboarding.route)
                    },
                    onUserSetDefault = {
                        callsViewModel.markUserSetDefault()
                    }
                )
            }
            composable(Screen.Scan.route) {
                ScanScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            composable(Screen.AI.route) {
                AIScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            composable(Screen.Security.route) {
                SecurityScreen(
                    onNavigate = { screen ->
                        navController.navigate(screen.route)
                    }
                )
            }
            composable(Screen.LinkSecurity.route) {
                LinkSecurityScreen()
            }
            composable(Screen.MessageSecurity.route) {
                MessageSecurityScreen()
            }
            composable(Screen.ProSubscription.route) {
                ProSubscriptionScreen()
            }
            composable(Screen.Privacy.route) {
                PrivacyScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Learn.route) {
                LearnScreen()
            }
            composable(Screen.Notifications.route) {
                NotificationsPlaceholder()
            }
            composable(Screen.Profile.route) {
                ProfilePlaceholder()
            }
            composable(Screen.AllActions.route) {
                AllActionsPlaceholder()
            }
            composable(Screen.ActivityHistory.route) {
                ActivityHistoryPlaceholder()
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.DefaultDialerOnboarding.route) {
                DefaultDialerOnboardingScreen(
                    onComplete = { navController.popBackStack() },
                    onSkip = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun NotificationsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No new notifications",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )
    }
}

@Composable
fun ProfilePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "User profile coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )
    }
}

@Composable
fun AllActionsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "All Security Actions",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All security actions coming soon",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )
    }
}

@Composable
fun ActivityHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Activity History",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No security activity yet",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )
    }
}

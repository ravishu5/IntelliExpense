package com.intelliexpense.app.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intelliexpense.app.IntelliExpenseApp
import com.intelliexpense.app.ui.screens.AnalyticsScreen
import com.intelliexpense.app.ui.screens.AskYourMoneyScreen
import com.intelliexpense.app.ui.screens.DashboardScreen
import com.intelliexpense.app.ui.screens.ManageHubScreen
import com.intelliexpense.app.ui.screens.OnboardingScreen
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import com.intelliexpense.app.ui.screens.PrivacyCenterScreen
import com.intelliexpense.app.ui.screens.TransactionsScreen
import com.intelliexpense.app.ui.theme.IntelliExpenseTheme
import com.intelliexpense.app.ui.theme.LocalAppColors
import com.intelliexpense.app.ui.theme.ThemeMode

sealed class Screen(val title: String, val icon: ImageVector) {
    object Dashboard : Screen("Dashboard", Icons.Default.Dashboard)
    object Transactions : Screen("Ledger", Icons.AutoMirrored.Filled.ReceiptLong)
    object Analytics : Screen("Analytics", Icons.Default.Analytics)
    object AskAi : Screen("Ask Money", Icons.Default.AutoAwesome)
    object Manage : Screen("Manage", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as IntelliExpenseApp
        val prefs = getSharedPreferences("intelliexpense_settings", Context.MODE_PRIVATE)
        val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)
        val savedThemeId = prefs.getString("selected_theme", ThemeMode.CYBER_OBSIDIAN.id)

        setContent {
            var currentTheme by remember { mutableStateOf(ThemeMode.fromId(savedThemeId)) }

            IntelliExpenseTheme(themeMode = currentTheme) {
                val colors = com.intelliexpense.app.ui.theme.LocalAppColors.current
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colors.background
                ) {
                    var isCompleted by remember { mutableStateOf(isOnboardingCompleted) }

                    if (!isCompleted) {
                        OnboardingScreen(
                            onFinish = {
                                prefs.edit().putBoolean("onboarding_completed", true).apply()
                                isCompleted = true
                            }
                        )
                    } else {
                        MainAppContent(
                            app = app,
                            currentTheme = currentTheme,
                            onThemeSelected = { newTheme ->
                                currentTheme = newTheme
                                prefs.edit().putString("selected_theme", newTheme.id).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    app: IntelliExpenseApp,
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPrivacyCenter by remember { mutableStateOf(false) }
    var filterUnreviewedInTransactions by remember { mutableStateOf(false) }
    val colors = com.intelliexpense.app.ui.theme.LocalAppColors.current

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Analytics,
        Screen.AskAi,
        Screen.Manage
    )

    if (showPrivacyCenter) {
        PrivacyCenterScreen(
            privacyManager = app.privacyManager,
            repository = app.repository,
            currentTheme = currentTheme,
            onThemeSelected = onThemeSelected,
            onBack = { showPrivacyCenter = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = colors.surface,
                    contentColor = colors.primary
                ) {
                    navItems.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                if (index == 1) {
                                    filterUnreviewedInTransactions = false
                                }
                                selectedTab = index
                            },
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(screen.title, fontSize = 11.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (colors.isDark) Color.Black else Color.White,
                                selectedTextColor = colors.primary,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.primary
                            )
                        )
                    }
                }
            },
            containerColor = colors.background
        ) { paddingValues ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        repository = app.repository,
                        onNavigateToTransactions = { selectedTab = 1 },
                        onNavigateToAi = { selectedTab = 3 },
                        onNavigateToReview = {
                            filterUnreviewedInTransactions = true
                            selectedTab = 1
                        }
                    )
                    1 -> TransactionsScreen(
                        repository = app.repository,
                        initialFilterUnreviewed = filterUnreviewedInTransactions
                    )
                    2 -> AnalyticsScreen(repository = app.repository)
                    3 -> AskYourMoneyScreen(
                        repository = app.repository,
                        aiEngine = app.aiEngine
                    )
                    4 -> ManageHubScreen(
                        repository = app.repository,
                        onNavigateToPrivacyCenter = { showPrivacyCenter = true }
                    )
                }
            }
        }
    }
}

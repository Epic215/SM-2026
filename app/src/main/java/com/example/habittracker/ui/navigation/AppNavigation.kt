package com.example.habittracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.ui.screens.calendar.CalendarScreen
import com.example.habittracker.ui.screens.details.HabitDetailsScreen
import com.example.habittracker.ui.screens.details.HabitDetailsViewModel
import com.example.habittracker.ui.screens.edit.HabitEditScreen
import com.example.habittracker.ui.screens.edit.HabitEditViewModel
import com.example.habittracker.ui.screens.habits.HabitsScreen
import com.example.habittracker.ui.screens.stats.StatsScreen

private enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Habits("habits", "Nawyki", Icons.Outlined.CheckCircle),
    Calendar("calendar", "Kalendarz", Icons.Outlined.CalendarMonth),
    Stats("stats", "Statystyki", Icons.Outlined.BarChart),
}

private const val HABIT_EDIT_ROUTE = "habit/edit"
private const val HABIT_EDIT_ROUTE_PATTERN =
    "$HABIT_EDIT_ROUTE?${HabitEditViewModel.HABIT_ID_ARG}={${HabitEditViewModel.HABIT_ID_ARG}}"
private const val HABIT_DETAILS_ROUTE = "habit/details"
private const val HABIT_DETAILS_ROUTE_PATTERN =
    "$HABIT_DETAILS_ROUTE/{${HabitDetailsViewModel.HABIT_ID_ARG}}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination == null || TopLevelDestination.entries.any { destination ->
        currentDestination.hierarchy.any { it.route == destination.route }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Habits.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Habits.route) {
                HabitsScreen(
                    onAddHabit = { navController.navigate(HABIT_EDIT_ROUTE) },
                    onOpenHabit = { id -> navController.navigate("$HABIT_DETAILS_ROUTE/$id") },
                )
            }
            composable(TopLevelDestination.Calendar.route) { CalendarScreen() }
            composable(TopLevelDestination.Stats.route) { StatsScreen() }
            composable(
                route = HABIT_EDIT_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(HabitEditViewModel.HABIT_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
            ) {
                HabitEditScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = HABIT_DETAILS_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(HabitDetailsViewModel.HABIT_ID_ARG) { type = NavType.LongType }
                ),
            ) {
                HabitDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate("$HABIT_EDIT_ROUTE?${HabitEditViewModel.HABIT_ID_ARG}=$id")
                    },
                )
            }
        }
    }
}

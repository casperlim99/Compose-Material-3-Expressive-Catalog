package com.emertozd.compose.catalog.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.emertozd.compose.catalog.library.data.UserPreferencesRepository
import com.emertozd.compose.catalog.library.model.Components
import com.emertozd.compose.catalog.library.model.Theme
import com.emertozd.compose.catalog.library.ui.component.Component
import com.emertozd.compose.catalog.library.ui.example.Example
import com.emertozd.compose.catalog.library.ui.home.Home
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    initialFavoriteRoute: String?,
    theme: Theme,
    onThemeChange: (theme: Theme) -> Unit
) {
    val context = LocalContext.current
    val initialRoutes = remember(initialFavoriteRoute) { getInitialRoutes(initialFavoriteRoute) }
    val backStack = rememberNavBackStack(*initialRoutes.map { it as NavKey }.toTypedArray())
    val userPreferencesRepository = remember { UserPreferencesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Simple state to track the "current favorite" for UI indication. 
    // In a real serialization scenario, we would parse initialFavoriteRoute to a Route object.
    // For now, we just track the current route string for the "favorite" star toggle.
    var currentFavoriteString by rememberSaveable { mutableStateOf(initialFavoriteRoute) }

    // Helper to toggle favorite. We perform a crude string serialization for compatibility.
    fun toggleFavorite(routeString: String) {
        if (currentFavoriteString == routeString) {
            currentFavoriteString = null
        } else {
            currentFavoriteString = routeString
        }
        coroutineScope.launch {
            userPreferencesRepository.saveFavoriteRoute(currentFavoriteString)
        }
    }

    // Handle system back press
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        entryProvider = entryProvider {
            entry<Routes.HomeRoute> {
                Home(
                    components = Components,
                    theme = theme,
                    onThemeChange = onThemeChange,
                    onComponentClick = { component -> 
                        backStack.add(Routes.ComponentRoute(component.id)) 
                    },
                    favorite = currentFavoriteString == "home",
                    onFavoriteClick = { toggleFavorite("home") }
                )
            }

            entry<Routes.ComponentRoute> { route ->
                val component = Components.first { it.id == route.componentId }
                // Create a stable string key for this component route
                val routeString = "component/${component.id}"
                
                Component(
                    component = component,
                    theme = theme,
                    onThemeChange = onThemeChange,
                    onExampleClick = { example -> 
                        val index = component.examples.indexOf(example)
                        backStack.add(Routes.ExampleRoute(component.id, index)) 
                    },
                    onBackClick = { backStack.removeLastOrNull() },
                    favorite = currentFavoriteString == routeString,
                    onFavoriteClick = { toggleFavorite(routeString) }
                )
            }

            entry<Routes.ExampleRoute> { route ->
                val component = Components.first { it.id == route.componentId }
                val example = component.examples[route.exampleIndex]
                val routeString = "example/${component.id}/${route.exampleIndex}"

                Example(
                    component = component,
                    example = example,
                    theme = theme,
                    onThemeChange = onThemeChange,
                    onBackClick = { backStack.removeLastOrNull() },
                    favorite = currentFavoriteString == routeString,
                    onFavoriteClick = { toggleFavorite(routeString) }
                )
            }
        }
    )
    
}

private fun getInitialRoutes(route: String?): List<Routes> {
    if (route.isNullOrBlank() || route == "home") return listOf(Routes.HomeRoute)
    return try {
        val segments = route.split("/")
        when (segments.firstOrNull()) {
            "component" -> {
                val componentId = segments[1].toInt()
                listOf(Routes.HomeRoute, Routes.ComponentRoute(componentId))
            }
            "example" -> {
                val componentId = segments[1].toInt()
                val exampleIndex = segments[2].toInt()
                listOf(
                    Routes.HomeRoute,
                    Routes.ComponentRoute(componentId),
                    Routes.ExampleRoute(componentId, exampleIndex)
                )
            }
            else -> listOf(Routes.HomeRoute)
        }
    } catch (e: Exception) {
        listOf(Routes.HomeRoute)
    }
}

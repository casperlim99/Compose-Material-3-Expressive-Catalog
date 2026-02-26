/**
 * Navigation 3 Reference Implementation
 *
 * Demonstrates how to set up Jetpack Compose Navigation 3 with:
 * - NavDisplay as the host composable
 * - NavKey-based route definitions (see AppRoutes.kt)
 * - Material 3 Expressive motion transitions (slide + fade)
 * - Predictive back gesture support
 * - SaveableStateHolder and ViewModel entry decorators
 *
 * Dependencies required:
 *   - androidx.navigation3:navigation3-runtime
 *   - androidx.navigation3:navigation3-ui
 *   - androidx.lifecycle:lifecycle-viewmodel-navigation3
 *   - kotlinx.serialization (for @Serializable route classes)
 */
package com.emertozd.compose.catalog.navigation3

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Example of a full Navigation 3 setup with Material 3 Expressive transitions.
 *
 * Key concepts:
 * 1. [rememberNavBackStack] — creates a mutable back stack of [NavKey] routes.
 * 2. [NavDisplay] — the host composable that renders the current route.
 * 3. [entryProvider] — maps route types to composable content using `entry<T>`.
 * 4. [transitionSpec] / [popTransitionSpec] — define enter/exit animations.
 * 5. [predictivePopTransitionSpec] — defines the animation for predictive back gestures.
 * 6. Entry decorators — [rememberSaveableStateHolderNavEntryDecorator] for saved state,
 *    [rememberViewModelStoreNavEntryDecorator] for ViewModel scoping.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    // Create the back stack with an initial route
    val backStack = rememberNavBackStack(Routes.HomeRoute as NavKey)

    // Standard tween for deterministic slide animations (no shake on interruption)
    val spatialSpec = tween<IntOffset>(durationMillis = 350)

    // Material 3 Expressive motion specs for effects (fade, scale)
    val effectsEnterSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    val effectsExitSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
    val scaleSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },

        // Entry decorators provide SavedState and ViewModel support
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

        // Forward navigation: new screen slides in from right, old slides left slightly
        transitionSpec = {
            slideInHorizontally(animationSpec = spatialSpec) { it } togetherWith
                slideOutHorizontally(animationSpec = spatialSpec) { -it / 4 }
        },

        // Back navigation: inverse of forward
        popTransitionSpec = {
            slideInHorizontally(animationSpec = spatialSpec) { -it / 4 } togetherWith
                slideOutHorizontally(animationSpec = spatialSpec) { it }
        },

        // Predictive back gesture: same as pop for seamless gesture-to-animation handoff
        predictivePopTransitionSpec = {
            slideInHorizontally(animationSpec = spatialSpec) { -it / 4 } togetherWith
                slideOutHorizontally(animationSpec = spatialSpec) { it }
        },

        // Map route types to their composable screens
        entryProvider = entryProvider {
            entry<Routes.HomeRoute> {
                // Replace with your Home screen composable
                Text("Home Screen")
            }

            entry<Routes.ComponentRoute> { route ->
                // route.componentId is available here
                // Replace with your detail screen composable
                Text("Component Detail: ${route.componentId}")
            }

            entry<Routes.ExampleRoute> { route ->
                // route.componentId and route.exampleIndex are available here
                // Replace with your example screen composable
                Text("Example: ${route.componentId} / ${route.exampleIndex}")
            }
        }
    )
}

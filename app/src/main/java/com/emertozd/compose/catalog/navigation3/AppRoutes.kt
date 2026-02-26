package com.emertozd.compose.catalog.navigation3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object HomeRoute : NavKey, Routes

    @Serializable
    data class ComponentRoute(val componentId: Int) : NavKey, Routes

    @Serializable
    data class ExampleRoute(val componentId: Int, val exampleIndex: Int) : NavKey, Routes
}

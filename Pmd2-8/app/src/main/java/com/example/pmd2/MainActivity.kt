package com.example.pmd2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.pmd2.data.AppDatabase
import com.example.pmd2.opengl.GalaxyScreen
import com.example.pmd2.opengl.PlanetInfoScreen
import com.example.pmd2.ui.news.NewsScreen
import com.example.pmd2.ui.news.NewsViewModel
import com.example.pmd2.ui.news.NewsViewModelFactory
import com.example.pmd2.ui.theme.Pmd2Theme
import com.example.pmd2.opengl.PlanetDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.get(applicationContext).likeDao()

        val factory = NewsViewModelFactory(dao)

        setContent {
            Pmd2Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "news") {
                    composable("news") {
                        val viewModel: NewsViewModel = viewModel(factory = factory)
                        NewsScreen(
                            viewModel = viewModel,
                            onGalaxyClick = { navController.navigate("galaxy") }
                        )
                    }

                    composable("galaxy") {
                        GalaxyScreen(
                            onDetailClick = { selectedIndex ->
                                navController.navigate("planet_detail/$selectedIndex")
                            },
                            onLunaClick = { selectedIndex ->
                                navController.navigate("planet_info/$selectedIndex")
                            }
                        )
                    }

                    composable(
                        route = "planet_info/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 9
                        PlanetInfoScreen(
                            selectedIndex = index,
                            onBackClick = { navController.popBackStack() }
                        )
                    }


                    composable(
                        route = "planet_detail/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        PlanetDetailScreen(
                            selectedIndex = index,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
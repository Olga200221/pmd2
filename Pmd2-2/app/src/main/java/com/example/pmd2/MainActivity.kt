package com.example.pmd2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pmd2.data.AppDatabase
import com.example.pmd2.ui.news.NewsScreen
import com.example.pmd2.ui.news.NewsViewModel
import com.example.pmd2.ui.news.NewsViewModelFactory
import com.example.pmd2.ui.theme.Pmd2Theme
import com.example.pmd2.opengl.GalaxyScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // получаем DAO
        val dao = AppDatabase.get(applicationContext).likeDao()

        // создаём фабрику для ViewModel
        val factory = NewsViewModelFactory(dao)

        setContent {
            Pmd2Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "news") {
                    // Экран новостей
                    composable("news") {
                        // создаём ViewModel через фабрику
                        val viewModel: NewsViewModel = viewModel(factory = factory)
                        NewsScreen(viewModel = viewModel, onGalaxyClick = {
                            navController.navigate("galaxy")
                        })
                    }

                    // Экран галактики (OpenGL)
                    composable("galaxy") {
                        GalaxyScreen()
                    }
                }
            }
        }
    }
}

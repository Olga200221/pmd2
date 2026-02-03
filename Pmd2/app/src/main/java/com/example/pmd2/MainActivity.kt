package com.example.pmd2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pmd2.data.AppDatabase
import com.example.pmd2.ui.news.NewsScreen
import com.example.pmd2.ui.news.NewsViewModel
import com.example.pmd2.ui.news.NewsViewModelFactory
import com.example.pmd2.ui.theme.Pmd2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // получаем DAO
        val dao = AppDatabase.get(applicationContext).likeDao()

        // создаём фабрику для ViewModel
        val factory = NewsViewModelFactory(dao)

        setContent {
            Pmd2Theme {
                // создаём ViewModel через фабрику
                val viewModel: NewsViewModel = viewModel(factory = factory)
                NewsScreen(viewModel)
            }
        }
    }
}

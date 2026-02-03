package com.example.pmd2.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pmd2.ui.theme.AccentLike
import com.example.pmd2.ui.theme.CardBackground

@Composable
fun NewsScreen(viewModel: NewsViewModel) {
    val news by viewModel.news.collectAsState()

    // Если новости ещё не загрузились — показать текст загрузки
    if (news.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Загрузка новостей...",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            for (row in 0 until 2) {
                Row(Modifier.weight(1f)) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        // Проверяем, что индекс существует
                        val newsItem = news.getOrNull(index)
                        if (newsItem != null) {
                            NewsBlock(
                                newsItem = newsItem,
                                onLike = { viewModel.likeNews(index) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Пустая карточка для выравнивания сетки
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsBlock(
    newsItem: NewsItem,
    onLike: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier
            .padding(8.dp)
            .background(CardBackground)
    ) {
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = newsItem.text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxWidth()
                .clickable { onLike() }
                .background(AccentLike),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "❤ ${newsItem.likes}",
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 14.sp
            )
        }
    }
}

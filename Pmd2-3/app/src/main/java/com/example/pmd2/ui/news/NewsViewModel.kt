package com.example.pmd2.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pmd2.data.LikeDao
import com.example.pmd2.data.LikeEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class NewsViewModel(
    private val dao: LikeDao
) : ViewModel() {

    private val allNews = listOf(
        0 to "Открыта новая экзопланета",
        1 to "Телескоп Джеймс Уэбб получил новые данные",
        2 to "Обнаружена самая старая галактика",
        3 to "Солнечная активность выросла",
        4 to "Марс готовится к новой миссии",
        5 to "Найдены следы воды на Европе",
        6 to "Астрономы изучают тёмную материю",
        7 to "Новая комета приближается к Земле",
        8 to "Созвездия меняют положение",
        9 to "Учёные обсуждают жизнь во Вселенной"
    )

    private val _news = MutableStateFlow<List<NewsItem>>(emptyList())
    val news: StateFlow<List<NewsItem>> = _news

    init {
        loadInitialNews()
        startAutoUpdate()
    }

    private fun loadInitialNews() {
        viewModelScope.launch {
            val selected = allNews.shuffled().take(4)
            val newsList = mutableListOf<NewsItem>()

            for ((id, text) in selected) {
                val likes = dao.getLikes(id) ?: 0
                newsList.add(NewsItem(id, text, likes))
            }

            _news.value = newsList
        }
    }

    private fun startAutoUpdate() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                replaceRandomNews()
            }
        }
    }

    private suspend fun replaceRandomNews() {
        val currentIds = _news.value.map { it.id }.toSet()
        val available = allNews.filterNot { it.first in currentIds }
        if (available.isEmpty()) return

        val (newId, newText) = available.random()
        val index = Random.nextInt(_news.value.size)
        val likes = dao.getLikes(newId) ?: 0

        val newList = _news.value.toMutableList()
        newList[index] = NewsItem(newId, newText, likes)
        _news.value = newList
    }

    fun likeNews(index: Int) {
        viewModelScope.launch {
            val item = _news.value[index]
            val newLikes = item.likes + 1
            dao.saveLikes(LikeEntity(item.id, newLikes))

            _news.value = _news.value.mapIndexed { i, n ->
                if (i == index) n.copy(likes = newLikes) else n
            }
        }
    }
}

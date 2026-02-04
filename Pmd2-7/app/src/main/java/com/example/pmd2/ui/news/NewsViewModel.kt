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
        0 to "Слабо возмущённое Солнце и такая же геомагнитная обстановка ожидают землян в воскресенье, сообщили в Лаборатории солнечной астрономии Института космических исследований (ИКИ) РАН.\n" +
                "\"Космическая погода 21 декабря 2025 года — СЛАБО ВОЗМУЩЕННАЯ ... Прогноз на сутки: Геомагнитная обстановка — возможны возмущения. Вспышечная активность — слабо повышенная без рисков для Земли\", - говорится в сообщении в Telegram-канале Лаборатории.",
        1 to "Гигантский протуберанец, превосходящий Землю по размерам в 80 раз, сформировался в ночь на 20 ноября на Солнце и оторвался от него, сообщили в лаборатории солнечной астрономии Института космических исследований (ИКИ) РАН.",
        2 to "Ночью на Земле начались сильные магнитные бури, сообщили в Лаборатории солнечной астрономии ИКИ РАН.\n" +
                "\"На Земле около полуночи по московскому времени зарегистрированы магнитные бури планетарного масштаба. В настоящее время геомагнитный индекс Kp находится в диапазоне от G2 до G3, то есть от средних до сильных бурь\"",
        3 to "Метеорный поток Ориониды достигнет пика 21 октября, в этом году для его наблюдения будут идеальные условия, сообщил крымский астроном Александр Якушечкин.\n" +
                "\"Двадцать первого октября в 15:26 по московскому времени состоится октябрьское новолуние. Этот период идеально подходит для астрономических наблюдений.",
        4 to "Земля уже попала под воздействие потока плазмы от корональной дыры на Солнце, хотя ранее прогнозировалось, что это произойдёт в воскресенье вечером, сообщили в Лаборатории солнечной астрономии Института космических исследований (ИКИ) РАН.",
        5 to "На Солнце почти полностью исчезли пятна, это говорит об уменьшении числа вспышек, сообщили в Лаборатории солнечной астрономии ИКИ РАН.\n" +
                "\"Наиболее примечательной особенностью последних дней является почти полное исчезновение пятен на обращенной к Земле стороне Солнца. Судя по всему, то же самое происходит сейчас и на обратной стороне\"",
        6 to "Светящееся туманное облако, которое можно было видеть в вечернем небе над южными регионами России, стало следствием запуска ракеты из Калифорнии, рассказал РИА",
        7 to "В небе над Астаной заметили летящие светящиеся объекты, которые могут быть вызваны сгоранием фрагментов космического аппарата, сказал РИА ",
        8 to "Ученые смогли заранее определить появление упавшего в Якутии астероида и рассчитать траекторию его движения, что поистине уникальное",
        9 to "Вблизи орбиты Земли пролетят два потенциально опасных астероида, пишет сообщает Newsweek со ссылкой на данные NASA.\n" +
                "Космические тела получили названия 2007 JX2 и 2020 XR. Они имеют размеры 402,3 и 387,7 метра соответственно. По оценкам ученых, оба астероида сопоставимы с высотой небоскреба Эмпайр-стейт-билдинг в Нью-Йорке (381 метр)."
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

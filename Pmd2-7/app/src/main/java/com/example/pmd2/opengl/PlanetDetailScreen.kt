package com.example.pmd2.opengl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pmd2.R

@Composable
fun PlanetDetailScreen(
    selectedIndex: Int,
    onBackClick: () -> Unit = {}  // ← добавлен параметр для возврата назад
) {
    // Порядок соответствует индексам: 0=Солнце, 1=Меркурий, ..., 8=Нептун, 9=Луна (но Луна сюда не попадает)
    val titles = listOf(
        "Солнце", "Меркурий", "Венера", "Земля", "Марс",
        "Юпитер", "Сатурн", "Уран", "Нептун"
    )

    val descriptions = listOf(
        "Солнце — центральная звезда Солнечной системы. Диаметр ≈ 1,39 млн км, температура поверхности ≈ 5500 °C. Источник света и тепла для всех планет.",
        "Меркурий — ближайшая к Солнцу планета. Диаметр 4879 км, очень большие перепады температур: +427 °C днём и -173 °C ночью.",
        "Венера — вторая планета от Солнца. Самая горячая (≈ 464 °C) из-за мощного парникового эффекта. Атмосфера почти полностью из CO₂.",
        "Земля — третья планета. Единственная известная планета с жизнью. Диаметр 12 742 км, 71% поверхности покрыто водой.",
        "Марс — четвёртая планета. Диаметр 6779 км. Имеет тонкую атмосферу, полярные шапки из льда и самый высокий вулкан в Солнечной системе — Олимп.",
        "Юпитер — пятая планета, крупнейшая в Солнечной системе. Газовый гигант с массой в 318 раз больше Земли. Имеет Большое красное пятно — гигантский шторм.",
        "Сатурн — шестая планета. Известен своей системой колец. Газовый гигант, плотность меньше воды — теоретически мог бы плавать.",
        "Уран — седьмая планета. Ледяной гигант, ось вращения наклонена почти на 98°. Имеет слабые кольца и 27 известных спутников.",
        "Нептун — восьмая и самая дальняя планета. Ледяной гигант с самыми сильными ветрами (до 2100 км/ч). Тёмно-синий цвет из-за метана."
    )

    val images = listOf(
        R.drawable.sun_i,
        R.drawable.mercury_i,
        R.drawable.venus_i,
        R.drawable.earth_i,
        R.drawable.mars_i,
        R.drawable.jupiter_i,
        R.drawable.saturn_i,
        R.drawable.uranus_i,
        R.drawable.neptune_i
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка "Назад" вверху экрана
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onBackClick) {
                Text("Назад")
            }
        }

        if (selectedIndex in titles.indices) {
            Text(
                text = titles[selectedIndex],
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Image(
                painter = painterResource(images[selectedIndex]),
                contentDescription = titles[selectedIndex],
                modifier = Modifier
                    .size(280.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = descriptions[selectedIndex],
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Text("Ошибка: неизвестный индекс $selectedIndex")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
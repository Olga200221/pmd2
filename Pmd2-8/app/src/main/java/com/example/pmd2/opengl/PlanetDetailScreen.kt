package com.example.pmd2.opengl

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pmd2.R

@Composable
fun PlanetDetailScreen(
    selectedIndex: Int,
    onBackClick: () -> Unit = {}
) {
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
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFFD1D3D0),
                    fontSize = 32.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(images[selectedIndex]),
                    contentDescription = titles[selectedIndex],
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = descriptions[selectedIndex],
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFB78FB1),
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Text(
                text = "Ошибка: неизвестный индекс $selectedIndex",
                color = Color.Red,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
package com.example.moiorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray

data class Section(
    val title: String,
    val icon: ImageVector,
    val hint: String
)

val sections = listOf(
    Section("Покупки", Icons.Default.ShoppingCart, "Список покупок"),
    Section("Финансы", Icons.Default.AccountBalanceWallet, "Доходы и расходы"),
    Section("Календарь", Icons.Default.CalendarMonth, "События и планы"),
    Section("Напоминания", Icons.Default.Notifications, "Важные дела"),
    Section("Имущество", Icons.Default.Inventory2, "Техника и вещи"),
    Section("Документы", Icons.Default.Folder, "Чеки и гарантии"),
    Section("Дом", Icons.Default.Home, "Домашние задачи"),
    Section("Заметки", Icons.Default.EditNote, "Идеи и записи")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrganizerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("organizer_data", 0)
    }

    var selected by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }

    val entries = remember(selected, refresh) {
        if (selected == null) {
            emptyList()
        } else {
            val array = JSONArray(
                prefs.getString(selected, "[]") ?: "[]"
            )
            (0 until array.length()).map {
                array.getString(it)
            }
        }
    }

    val green = Color(0xFF356B61)
    val background = Color(0xFFF6F7F9)

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = green,
            background = background
        )
    ) {
        Scaffold(
            containerColor = background,

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            selected ?: "Мой органайзер",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (selected != null) {
                            IconButton(
                                onClick = {
                                    selected = null
                                    input = ""
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    "Назад"
                                )
                            }
                        }
                    }
                )
            },

            bottomBar = {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    val labels = listOf(
                        "Главная",
                        "Календарь",
                        "Дела",
                        "Заметки",
                        "Ещё"
                    )

                    val icons = listOf(
                        Icons.Default.GridView,
                        Icons.Default.CalendarMonth,
                        Icons.Default.CheckCircle,
                        Icons.Default.EditNote,
                        Icons.Default.MoreHoriz
                    )

                    labels.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = {
                                tab = index
                                selected = when (index) {
                                    1 -> "Календарь"
                                    2 -> "Напоминания"
                                    3 -> "Заметки"
                                    else -> null
                                }
                            },
                            icon = {
                                Icon(icons[index], label)
                            },
                            label = {
                                Text(label)
                            }
                        )
                    }
                }
            }
        ) { padding ->

            if (selected == null) {

                LazyColumn(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            "Всё важное — в одном месте",
                            color = Color(0xFF68727D),
                            modifier = Modifier.padding(
                                top = 4.dp,
                                bottom = 18.dp
                            )
                        )

                        Text(
                            "Мои разделы",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    items(sections.chunked(2)) { pair ->

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { section ->

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(132.dp)
                                        .clickable {
                                            selected = section.title
                                            tab = 4
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            section.icon,
                                            null,
                                            tint = green,
                                            modifier = Modifier.size(28.dp)
                                        )

                                        Column {
                                            Text(
                                                section.title,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Text(
                                                section.hint,
                                                color = Color(0xFF7B858E),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Данные хранятся только на этом телефоне",
                            color = Color(0xFF8A939B),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

            } else {

                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    Text(
                        sections.firstOrNull {
                            it.title == selected
                        }?.hint ?: "Ваши записи",
                        color = Color(0xFF68727D)
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Новая запись")
                        },
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (input.isNotBlank()) {
                                        val array = JSONArray(
                                            prefs.getString(selected, "[]") ?: "[]"
                                        )

                                        array.put(input.trim())

                                        prefs.edit()
                                            .putString(selected, array.toString())
                                            .apply()

                                        input = ""
                                        refresh++
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    "Добавить"
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    if (entries.isEmpty()) {

                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Пока пусто. Добавьте первую запись.",
                                color = Color.Gray
                            )
                        }

                    } else {

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(entries) { entry ->

                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 14.dp,
                                                end = 6.dp,
                                                top = 6.dp,
                                                bottom = 6.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            entry,
                                            Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = {
                                                val array = JSONArray()

                                                entries
                                                    .filter { it != entry }
                                                    .forEach { array.put(it) }

                                                prefs.edit()
                                                    .putString(selected, array.toString())
                                                    .apply()

                                                refresh++
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                "Удалить",
                                                tint = Color(0xFF9A6262)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

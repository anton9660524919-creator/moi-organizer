
package com.example.moiorganizer

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*

class MainActivity : Activity() {

    private lateinit var prefs: android.content.SharedPreferences

    private val bg = Color.rgb(250, 250, 250)
    private val cardColor = Color.rgb(242, 242, 242)
    private val dark = Color.rgb(22, 24, 27)
    private val gray = Color.rgb(105, 105, 105)
    private val green = Color.rgb(65, 150, 105)

    private val sections = listOf(
        "🛒 Покупки",
        "💰 Финансы",
        "📅 Календарь",
        "🔔 Напоминания",
        "📦 Имущество",
        "📁 Документы",
        "🏠 Дом",
        "📝 Заметки"
    )

    private val descriptions = listOf(
        "Списки покупок",
        "Доходы и расходы",
        "События и планы",
        "Важные даты",
        "Вещи и техника",
        "Чеки и гарантии",
        "Ремонт и обслуживание",
        "Идеи и записи"
    )

    private val symbols = listOf(
        "🛒", "👛", "📅", "🔔",
        "📦", "📄", "🏠", "📝"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(
            "organizer_data",
            Context.MODE_PRIVATE
        )

        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        showHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun page(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            fitsSystemWindows = true
        }
    }

    private fun showHome() {
        val root = page()

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = text("▦  Мой органайзер", 21f, dark, true)
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val badge = text("Главная", 14f, green, true).apply {
            background = rounded(Color.rgb(225, 242, 232), 30)
            setPadding(dp(13), dp(8), dp(13), dp(8))
        }
        top.addView(badge)
        content.addView(top)

        val greeting = text("Добрый день!", 32f, dark, true)
        greeting.setPadding(0, dp(35), 0, dp(5))
        content.addView(greeting)

        val subtitle = text("Всё важное — в одном месте", 17f, gray)
        subtitle.setPadding(0, 0, 0, dp(22))
        content.addView(subtitle)

        for (i in 0 until 4) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            for (j in 0..1) {
                val index = i * 2 + j

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL
                    background = rounded(cardColor, 22)
                    setPadding(dp(18), dp(15), dp(12), dp(13))
                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        showSection(sections[index])
                    }
                }

                val icon = text(symbols[index], 29f, dark)
                card.addView(icon)

                val title = text(
                    sections[index].substringAfter(" "),
                    19f,
                    dark,
                    true
                )
                title.setPadding(0, dp(9), 0, dp(6))
                card.addView(title)

                val description = text(
                    descriptions[index],
                    15f,
                    gray
                )
                card.addView(description)

                val params = LinearLayout.LayoutParams(
                    0,
                    dp(145),
                    1f
                )

                params.setMargins(
                    if (j == 0) 0 else dp(7),
                    dp(6),
                    if (j == 0) dp(7) else 0,
                    dp(6)
                )

                row.addView(card, params)
            }

            content.addView(row)
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addBottomMenu(root)
        setContentView(root)
    }

    private fun addBottomMenu(root: LinearLayout) {
        val line = View(this).apply {
            setBackgroundColor(Color.rgb(230, 230, 230))
        }

        root.addView(
            line,
            LinearLayout.LayoutParams(-1, dp(1))
        )

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(bg)
            setPadding(dp(5), dp(7), dp(5), dp(7))
        }

        val actions = listOf(
            Pair("🏠", "Главная"),
            Pair("📅", "Календарь"),
            Pair("＋", "Добавить"),
            Pair("🔔", "Напоминания"),
            Pair("⚙️", "Настройки")
        )

        actions.forEachIndexed { index, item ->
            val button = TextView(this).apply {
                text = item.first
                textSize = if (index == 2) 30f else 24f
                gravity = Gravity.CENTER
                setTextColor(dark)
                isClickable = true
                isFocusable = true
                setPadding(0, dp(5), 0, dp(5))

                setOnClickListener {
                    when (index) {
                        0 -> showHome()
                        1 -> showSection("📅 Календарь")
                        2 -> showAddDialog()
                        3 -> showSection("🔔 Напоминания")
                        4 -> showSettings()
                    }
                }
            }

            menu.addView(
                button,
                LinearLayout.LayoutParams(0, dp(52), 1f)
            )
        }

        root.addView(menu)
    }

    private fun showSection(section: String) {
        val root = page()

        val header = text(section, 25f, dark, true)
        header.setPadding(dp(20), dp(22), dp(16), dp(18))
        root.addView(header)

        val input = EditText(this).apply {
            hint = "Новая запись"
            setSingleLine(true)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }

        root.addView(
            input,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(dp(14), dp(5), dp(14), dp(5))
            }
        )

        val add = Button(this).apply {
            text = "＋ Добавить"
            isAllCaps = false

            setOnClickListener {
                val value = input.text.toString().trim()

                if (value.isNotEmpty()) {
                    val list = loadItems(section).toMutableList()
                    list.add(value)
                    saveItems(section, list)
                    showSection(section)
                }
            }
        }

        root.addView(add)

        val scroll = ScrollView(this)
        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val items = loadItems(section)

        if (items.isEmpty()) {
            val empty = text("Пока здесь пусто", 16f, gray)
            empty.gravity = Gravity.CENTER
            empty.setPadding(dp(12), dp(30), dp(12), dp(30))
            listLayout.addView(empty)
        }

        items.forEachIndexed { index, value ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val itemText = text(value, 16f, dark)
            itemText.setPadding(dp(12), dp(14), dp(8), dp(14))

            row.addView(
                itemText,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            val delete = Button(this).apply {
                text = "×"

                setOnClickListener {
                    val updated = loadItems(section).toMutableList()
                    updated.removeAt(index)
                    saveItems(section, updated)
                    showSection(section)
                }
            }

            row.addView(delete)
            listLayout.addView(row)

            val divider = View(this).apply {
                setBackgroundColor(Color.LTGRAY)
            }

            listLayout.addView(
                divider,
                LinearLayout.LayoutParams(-1, dp(1))
            )
        }

        scroll.addView(listLayout)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addBottomMenu(root)
        setContentView(root)
    }

    private fun showAddDialog() {
        val names = sections.map {
            it.substringAfter(" ")
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Куда добавить запись?")
            .setItems(names) { _, which ->
                showSection(sections[which])
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showSettings() {
        val root = page()

        val header = text("Настройки", 25f, dark, true)
        header.setPadding(dp(20), dp(22), dp(16), dp(18))
        root.addView(header)

        val info = text(
            "Мой органайзер\n\nВсе данные хранятся на этом телефоне.\nПриложение работает без интернета.",
            16f,
            gray
        )

        info.setPadding(dp(20), dp(20), dp(20), dp(20))
        root.addView(info)

        root.addView(
            View(this),
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addBottomMenu(root)
        setContentView(root)
    }

    private fun loadItems(section: String): List<String> {
        val saved = prefs.getString(section, "") ?: ""

        if (saved.isEmpty()) return emptyList()

        return saved.split("\n")
    }

    private fun saveItems(section: String, items: List<String>) {
        prefs.edit()
            .putString(section, items.joinToString("\n"))
            .apply()
    }
}

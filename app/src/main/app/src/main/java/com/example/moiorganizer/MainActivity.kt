package com.example.moiorganizer

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.content.Context

class MainActivity : Activity() {

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

    private lateinit var content: LinearLayout
    private lateinit var prefs: android.content.SharedPreferences

    private val green = Color.rgb(53, 107, 97)
    private val background = Color.rgb(246, 247, 249)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("organizer_data", Context.MODE_PRIVATE)

        showHome()
    }

    private fun baseLayout(title: String): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(background)

        val header = TextView(this)
        header.text = title
        header.textSize = 25f
        header.setTextColor(Color.rgb(32, 37, 43))
        header.setTypeface(null, Typeface.BOLD)
        header.setPadding(20, 22, 16, 18)

        root.addView(header)

        return root
    }

    private fun makeButton(text: String, action: () -> Unit): Button {
        val button = Button(this)
        button.text = text
        button.setOnClickListener { action() }
        return button
    }

    private fun showHome() {
        val root = baseLayout("Мой органайзер")

        val subtitle = TextView(this)
        subtitle.text = "Всё важное — в одном месте"
        subtitle.textSize = 15f
        subtitle.setTextColor(Color.GRAY)
        subtitle.setPadding(20, 0, 20, 16)
        root.addView(subtitle)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        grid.setPadding(12, 8, 12, 12)

        for (i in sections.indices step 2) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            for (j in i until minOf(i + 2, sections.size)) {
                val button = Button(this)
                button.text = sections[j]
                button.textSize = 15f
                button.isAllCaps = false
                button.setBackgroundColor(Color.WHITE)

                val params = LinearLayout.LayoutParams(
                    0,
                    115,
                    1f
                )
                params.setMargins(5, 5, 5, 5)

                row.addView(button, params)

                val section = sections[j]
                button.setOnClickListener {
                    showSection(section)
                }
            }

            grid.addView(row)
        }

        scroll.addView(grid)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val footer = TextView(this)
        footer.text = "Данные хранятся на этом телефоне"
        footer.gravity = Gravity.CENTER
        footer.setTextColor(Color.GRAY)
        footer.setPadding(8, 12, 8, 16)
        root.addView(footer)

        setContentView(root)
    }

    private fun showSection(section: String) {
        val root = baseLayout(section)

        root.addView(
            makeButton("← На главную") {
                showHome()
            }
        )

        val input = EditText(this)
        input.hint = "Новая запись"
        input.setSingleLine(true)
        input.setPadding(16, 12, 16, 12)

        root.addView(input)

        root.addView(
            makeButton("＋ Добавить") {
                val value = input.text.toString().trim()

                if (value.isNotEmpty()) {
                    val list = loadItems(section).toMutableList()
                    list.add(value)
                    saveItems(section, list)
                    showSection(section)
                }
            }
        )

        val scroll = ScrollView(this)
        val listLayout = LinearLayout(this)
        listLayout.orientation = LinearLayout.VERTICAL
        listLayout.setPadding(12, 8, 12, 8)

        val items = loadItems(section)

        if (items.isEmpty()) {
            val empty = TextView(this)
            empty.text = "Пока здесь пусто"
            empty.textSize = 16f
            empty.gravity = Gravity.CENTER
            empty.setPadding(12, 30, 12, 30)
            listLayout.addView(empty)
        }

        items.forEachIndexed { index, value ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL

            val text = TextView(this)
            text.text = value
            text.textSize = 16f
            text.setTextColor(Color.DKGRAY)
            text.setPadding(12, 14, 8, 14)

            row.addView(
                text,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            val delete = Button(this)
            delete.text = "×"
            delete.setOnClickListener {
                val updated = loadItems(section).toMutableList()
                updated.removeAt(index)
                saveItems(section, updated)
                showSection(section)
            }

            row.addView(delete)
            listLayout.addView(row)

            val divider = android.view.View(this)
            divider.setBackgroundColor(Color.LTGRAY)
            listLayout.addView(
                divider,
                LinearLayout.LayoutParams(-1, 1)
            )
        }

        scroll.addView(listLayout)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

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

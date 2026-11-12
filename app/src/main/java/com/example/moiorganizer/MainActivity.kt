
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
import android.widget.*
import android.text.InputType
import org.json.JSONArray
import org.json.JSONObject

data class ShoppingItem(
    val name: String,
    val quantity: String,
    val bought: Boolean
)

class MainActivity : Activity() {

    private lateinit var prefs: android.content.SharedPreferences
    private var currentScreen = "home"

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

    private val shoppingItems = mutableListOf<ShoppingItem>()
    private var shoppingFilter = "Купить"

    private lateinit var shoppingList: LinearLayout
    private lateinit var shoppingNameInput: EditText
    private lateinit var shoppingQtyInput: EditText

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

    override fun onBackPressed() {
        if (currentScreen != "home") {
            showHome()
        } else {
            super.onBackPressed()
        }
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

    // Главный экран

    private fun showHome() {
        currentScreen = "home"

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

        top.addView(
            text("▦  Мой органайзер", 21f, dark, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

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

                card.addView(text(symbols[index], 29f, dark))

                val title = text(
                    sections[index].substringAfter(" "),
                    19f,
                    dark,
                    true
                )
                title.setPadding(0, dp(9), 0, dp(6))
                card.addView(title)

                card.addView(text(descriptions[index], 15f, gray))

                val params = LinearLayout.LayoutParams(0, dp(145), 1f)
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
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addBottomMenu(root)
        setContentView(root)
    }

    // Нижнее меню

    private fun addBottomMenu(root: LinearLayout) {
        val line = View(this).apply {
            setBackgroundColor(Color.rgb(230, 230, 230))
        }

        root.addView(line, LinearLayout.LayoutParams(-1, dp(1)))

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

            menu.addView(button, LinearLayout.LayoutParams(0, dp(52), 1f))
        }

        root.addView(menu)
    }

    // Остальные разделы

    private fun showSection(section: String) {
        if (section == "🛒 Покупки") {
            showShopping()
            return
        }

        currentScreen = "section"

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
                } else {
                    input.error = "Введите текст"
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
                background = rounded(Color.WHITE, 12)
                setPadding(dp(10), dp(5), dp(5), dp(5))
            }

            val itemText = text(value, 16f, dark)
            itemText.setPadding(dp(8), dp(12), dp(8), dp(12))

            row.addView(
                itemText,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            row.addView(sectionAction("✎") {
                editSectionItem(section, index)
            })

            row.addView(sectionAction("×") {
                confirmDeleteSectionItem(section, index)
            })

            val rowParams = LinearLayout.LayoutParams(-1, -2)
            rowParams.bottomMargin = dp(7)
            listLayout.addView(row, rowParams)
        }

        scroll.addView(listLayout)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addBottomMenu(root)
        setContentView(root)
    }

    // Кнопки редактирования и удаления записей

    private fun sectionAction(
        symbol: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(gray)
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnClickListener { action() }
        }
    }

    private fun editSectionItem(section: String, index: Int) {
        val items = loadItems(section).toMutableList()

        if (index !in items.indices) return

        val input = EditText(this).apply {
            setText(items[index])
            hint = "Текст записи"
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        AlertDialog.Builder(this)
            .setTitle("Редактировать запись")
            .setView(input)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                val value = input.text.toString().trim()

                if (value.isNotEmpty()) {
                    val updated = loadItems(section).toMutableList()

                    if (index in updated.indices) {
                        updated[index] = value
                        saveItems(section, updated)
                        showSection(section)
                    }
                }
            }
            .show()
    }

    private fun confirmDeleteSectionItem(section: String, index: Int) {
        val items = loadItems(section)

        if (index !in items.indices) return

        AlertDialog.Builder(this)
            .setTitle("Удалить запись?")
            .setMessage(items[index])
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                val updated = loadItems(section).toMutableList()

                if (index in updated.indices) {
                    updated.removeAt(index)
                    saveItems(section, updated)
                    showSection(section)
                }
            }
            .show()
    }

    // Покупки

    private fun loadShopping() {
        shoppingItems.clear()

        val data = prefs.getString("shopping_items_v1", null)

        if (data != null) {
            try {
                val array = JSONArray(data)

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    shoppingItems.add(
                        ShoppingItem(
                            obj.optString("name"),
                            obj.optString("quantity", "1"),
                            obj.optBoolean("bought", false)
                        )
                    )
                }
            } catch (_: Exception) {
                shoppingItems.clear()
            }
        } else {
            val oldItems = loadItems("🛒 Покупки")

            oldItems.forEach {
                shoppingItems.add(ShoppingItem(it, "1", false))
            }

            saveShopping()
        }
    }

    private fun saveShopping() {
        val array = JSONArray()

        shoppingItems.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("quantity", it.quantity)
            obj.put("bought", it.bought)
            array.put(obj)
        }

        prefs.edit()
            .putString("shopping_items_v1", array.toString())
            .apply()
    }

    private fun showShopping() {
        currentScreen = "shopping"
        loadShopping()

        val root = page()

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            text("Мои покупки", 25f, dark, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        header.addView(text("🛒", 25f, dark))
        content.addView(header)

        content.addView(
            text("Ваш список покупок", 13f, gray).apply {
                setPadding(0, dp(4), 0, dp(16))
            }
        )

        val inputCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.WHITE, 16)
        }

        shoppingNameInput = shoppingField("Название товара")

        inputCard.addView(
            shoppingNameInput,
            LinearLayout.LayoutParams(-1, dp(52))
        )

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        shoppingQtyInput = shoppingField("Количество").apply {
            setText("1")
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val qtyParams = LinearLayout.LayoutParams(0, dp(56), 1f)
        qtyParams.marginEnd = dp(8)

        inputRow.addView(shoppingQtyInput, qtyParams)

        val addButton = TextView(this).apply {
            text = "＋ Добавить"
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = rounded(dark, 12)
            setPadding(dp(14), dp(6), dp(14), dp(6))
            setOnClickListener { addShoppingItem() }
        }

        inputRow.addView(
            addButton,
            LinearLayout.LayoutParams(-2, dp(56))
        )

        inputCard.addView(inputRow)

        val cardParams = LinearLayout.LayoutParams(-1, -2)
        cardParams.bottomMargin = dp(14)

        content.addView(inputCard, cardParams)

        val filters = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        listOf("Купить", "Все", "Куплено").forEach { filter ->
            val button = TextView(this).apply {
                text = filter
                textSize = 14f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)

                val selected = shoppingFilter == filter

                setTextColor(if (selected) Color.WHITE else dark)
                background = rounded(
                    if (selected) dark else Color.WHITE,
                    12
                )

                setPadding(dp(5), dp(13), dp(5), dp(13))

                setOnClickListener {
                    shoppingFilter = filter
                    showShopping()
                }
            }

            val lp = LinearLayout.LayoutParams(0, dp(46), 1f)
            lp.marginEnd = dp(5)

            filters.addView(button, lp)
        }

        content.addView(filters)

        content.addView(
            text("Список товаров", 18f, dark, true).apply {
                setPadding(0, dp(18), 0, dp(10))
            }
        )

        val scroll = ScrollView(this)

        shoppingList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(shoppingList)

        content.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val clearButton = TextView(this).apply {
            text = "Удалить купленные"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(gray)
            setPadding(dp(8), dp(14), dp(8), dp(8))

            setOnClickListener {
                if (shoppingItems.any { it.bought }) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Удалить купленные товары?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить") { _, _ ->
                            shoppingItems.removeAll { it.bought }
                            saveShopping()
                            drawShoppingItems()
                        }
                        .show()
                }
            }
        }

        content.addView(clearButton)

        root.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addBottomMenu(root)
        setContentView(root)

        drawShoppingItems()
    }

    private fun shoppingField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(14), dp(6), dp(14), dp(6))
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.rgb(243, 244, 246), 12)
            setTextColor(dark)
        }
    }

    private fun addShoppingItem() {
        val name = shoppingNameInput.text.toString().trim()

        if (name.isEmpty()) {
            shoppingNameInput.error = "Введите название"
            return
        }

        val quantity = shoppingQtyInput.text
            .toString()
            .trim()
            .ifEmpty { "1" }

        shoppingItems.add(
            ShoppingItem(name, quantity, false)
        )

        shoppingFilter = "Купить"
        saveShopping()
        showShopping()
    }

    private fun drawShoppingItems() {
        shoppingList.removeAllViews()

        val shown = shoppingItems.withIndex().filter { indexed ->
            when (shoppingFilter) {
                "Купить" -> !indexed.value.bought
                "Куплено" -> indexed.value.bought
                else -> true
            }
        }

        if (shown.isEmpty()) {
            val message = when (shoppingFilter) {
                "Купить" -> "Все покупки выполнены"
                "Куплено" -> "Пока ничего не куплено"
                else -> "Список пока пуст"
            }
            val empty = text(message, 15f, gray).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(28), dp(12), dp(28))
            }
            shoppingList.addView(empty)
            return
        }

        shown.forEach { indexed ->
            val index = indexed.index
            val item = indexed.value

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(Color.WHITE, 14)
                setPadding(dp(8), dp(6), dp(5), dp(6))
            }

            val check = CheckBox(this).apply {
                isChecked = item.bought
                buttonTintList = android.content.res.ColorStateList.valueOf(green)
                setOnCheckedChangeListener { _, checked ->
                    if (index in shoppingItems.indices) {
                        shoppingItems[index] = shoppingItems[index].copy(bought = checked)
                        saveShopping()
                        drawShoppingItems()
                    }
                }
            }
            row.addView(check, LinearLayout.LayoutParams(-2, -2))

            val label = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(4), dp(5), dp(4), dp(5))
            }
            val nameView = text(item.name, 16f, dark, true)
            if (item.bought) nameView.paintFlags = nameView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            label.addView(nameView)
            label.addView(text("Количество: ${item.quantity}", 12f, gray))

            row.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(sectionAction("✎") { editShoppingItem(index) })
            row.addView(sectionAction("×") { confirmDeleteShoppingItem(index) })

            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(7)
            shoppingList.addView(row, params)
        }
    }

    private fun editShoppingItem(index: Int) {
        if (index !in shoppingItems.indices) return
        val old = shoppingItems[index]
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val nameInput = shoppingField("Название товара").apply { setText(old.name) }
        val qtyInput = shoppingField("Количество").apply { setText(old.quantity) }
        form.addView(nameInput)
        form.addView(qtyInput)

        AlertDialog.Builder(this)
            .setTitle("Изменить товар")
            .setView(form)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = nameInput.text.toString().trim()
                val quantity = qtyInput.text.toString().trim().ifEmpty { "1" }
                if (name.isNotEmpty() && index in shoppingItems.indices) {
                    shoppingItems[index] = shoppingItems[index].copy(name = name, quantity = quantity)
                    saveShopping()
                    drawShoppingItems()
                }
            }
            .show()
    }

    private fun confirmDeleteShoppingItem(index: Int) {
        if (index !in shoppingItems.indices) return
        AlertDialog.Builder(this)
            .setTitle("Удалить товар?")
            .setMessage(shoppingItems[index].name)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                if (index in shoppingItems.indices) {
                    shoppingItems.removeAt(index)
                    saveShopping()
                    drawShoppingItems()
                }
            }
            .show()
    }

    // Универсальное хранение записей остальных разделов

    private fun loadItems(section: String): List<String> {
        val result = mutableListOf<String>()
        val raw = prefs.getString("section_$section", null) ?: return result
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val value = array.optString(i, "")
                if (value.isNotBlank()) result.add(value)
            }
        } catch (_: Exception) {
            // Не перезаписываем повреждённые данные автоматически.
        }
        return result
    }

    private fun saveItems(section: String, items: List<String>) {
        val array = JSONArray()
        items.forEach { array.put(it) }
        prefs.edit().putString("section_$section", array.toString()).apply()
    }

    private fun showAddDialog() {
        val choices = sections.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Добавить запись")
            .setItems(choices) { _, which ->
                val section = choices[which]
                if (section == "🛒 Покупки") {
                    showShopping()
                } else {
                    showSection(section)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showSettings() {
        AlertDialog.Builder(this)
            .setTitle("Настройки")
            .setMessage("Мой органайзер\n\nДанные хранятся на этом устройстве. Приложение работает без подключения к интернету.")
            .setPositiveButton("Понятно", null)
            .show()
    }
}

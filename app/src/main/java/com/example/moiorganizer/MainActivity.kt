
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

            row.addView(itemText, LinearLayout.LayoutParams(0, -2, 1f))

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

            listLayout.addView(divider, LinearLayout.LayoutParams(-1, dp(1)))
        }

        scroll.addView(listLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        addBottomMenu(root)
        setContentView(root)
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
            // Перенос простых записей из прежнего раздела органайзера.
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

        content.addView(text("Ваш список покупок", 13f, gray).apply {
            setPadding(0, dp(4), 0, dp(16))
        })

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

        inputRow.addView(addButton, LinearLayout.LayoutParams(-2, dp(56)))
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
                background = rounded(if (selected) dark else Color.WHITE, 12)
                setPadding(dp(5), dp(13), dp(5), dp(13))
                setOnClickListener {
                    shoppingFilter = filter
                    drawShoppingItems()
                    showShopping()
                }
            }

            val lp = LinearLayout.LayoutParams(0, dp(46), 1f)
            lp.marginEnd = dp(5)
            filters.addView(button, lp)
        }

        content.addView(filters)

        content.addView(text("Список товаров", 18f, dark, true).apply {
            setPadding(0, dp(18), 0, dp(10))
        })

        val scroll = ScrollView(this)
        shoppingList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(shoppingList)
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val clearButton = TextView(this).apply {
            text = "Удалить купленные"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(gray)
            setPadding(dp(8), dp(14), dp(8), dp(8))
            setOnClickListener {
                shoppingItems.removeAll { it.bought }
                saveShopping()
                drawShoppingItems()
            }
        }

        content.addView(clearButton)

        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
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

        val quantity = shoppingQtyInput.text.toString().trim().ifEmpty { "1" }

        shoppingItems.add(ShoppingItem(name, quantity, false))
        shoppingFilter = "Купить"
        saveShopping()
        showShopping()
    }

    private fun drawShoppingItems() {
        shoppingList.removeAllViews()

        val shown = shoppingItems.withIndex().filter {
            shoppingFilter == "Все" ||
            (shoppingFilter == "Куплено" && it.value.bought) ||
            (shoppingFilter == "Купить" && !it.value.bought)
        }

        if (shown.isEmpty()) {
            shoppingList.addView(
                text("Список пока пуст", 15f, gray).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(28), 0, dp(20))
                }
            )
        }

        shown.forEach { indexed ->
            val index = indexed.index
            val item = indexed.value

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(8), dp(6), dp(8))
                background = rounded(Color.WHITE, 14)
            }

            val check = CheckBox(this).apply {
                isChecked = item.bought
                buttonTintList = android.content.res.ColorStateList.valueOf(
                    if (item.bought) Color.rgb(65, 155, 100) else dark
                )

                setOnCheckedChangeListener { _, checked ->
                    shoppingItems[index] = item.copy(bought = checked)
                    saveShopping()
                    drawShoppingItems()
                }
            }

            row.addView(check)

            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            val label = text(item.name, 16f, dark, true)
            if (item.bought) label.setTextColor(gray)
            info.addView(label)

            info.addView(text(item.quantity, 12f, gray))
            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

            row.addView(shoppingAction("✎") {
                editShoppingItem(index)
            })

            row.addView(shoppingAction("×") {
                shoppingItems.removeAt(index)
                saveShopping()
                drawShoppingItems()
            })

            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.bottomMargin = dp(7)
            shoppingList.addView(row, lp)
        }
    }

    private fun shoppingAction(symbol: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(gray)
            setPadding(dp(9), dp(4), dp(9), dp(4))
            setOnClickListener { action() }
        }
    }

    private fun editShoppingItem(index: Int) {
        val item = shoppingItems[index]

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }

        val name = EditText(this).apply {
           

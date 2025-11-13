package com.guardiant.app.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.guardiant.app.R
import kotlin.random.Random

/**
 * Componente reutilizable de teclado numérico para PIN
 * Con números ALEATORIOS para mayor seguridad
 *
 * Uso:
 * - En XML: <com.guardiant.app.ui.PinKeypadComponent android:id="@+id/pinKeypad" ... />
 * - En Kotlin: pinKeypad.onPinChanged = { pin -> /* hacer algo */ }
 */
class PinKeypadComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var keypadGrid: GridLayout
    private lateinit var pinDisplay: TextView
    private var pinBuilder = StringBuilder()

    // Mapeo de botón a número real
    private val numberMap = mutableMapOf<Button, String>()

    // Callback cuando cambia el PIN
    var onPinChanged: ((String) -> Unit)? = null
    var onPinComplete: ((String) -> Unit)? = null

    init {
        setupUI()
    }

    private fun setupUI() {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        // Inflamos el layout del teclado
        LayoutInflater.from(context).inflate(R.layout.component_pin_keypad, this, true)

        keypadGrid = findViewById(R.id.keypad)
        pinDisplay = findViewById(R.id.textPinDisplay)

        // Barajar números y asignarlos a los botones
        shuffleNumbers()

        // Configurar listeners en los botones
        for (i in 0 until keypadGrid.childCount) {
            val view = keypadGrid.getChildAt(i)
            if (view is Button) {
                view.setOnClickListener { onKeypadClick(it) }
            }
        }
    }

    /**
     * Barajar los números 0-9 y asignarlos aleatoriamente a los botones
     */
    private fun shuffleNumbers() {
        val numbers = (0..9).map { it.toString() }.toMutableList()
        numbers.shuffle()

        var numberIndex = 0

        for (i in 0 until keypadGrid.childCount) {
            val view = keypadGrid.getChildAt(i)
            if (view is Button) {
                val tag = view.tag.toString()

                // Si es el botón DEL, no tocamos
                if (tag == "del") {
                    continue
                }

                // Asignar un número aleatorio
                if (numberIndex < numbers.size) {
                    val randomNumber = numbers[numberIndex]
                    view.text = randomNumber
                    numberMap[view] = randomNumber // Guardar el mapeo real
                    numberIndex++
                }
            }
        }
    }

    private fun onKeypadClick(view: android.view.View) {
        if (view !is Button) return

        val tag = view.tag.toString()

        if (pinBuilder.length >= 6 && tag != "del") {
            return
        }

        when (tag) {
            "del" -> {
                if (pinBuilder.isNotEmpty()) {
                    pinBuilder.deleteCharAt(pinBuilder.length - 1)
                }
            }
            else -> {
                // Usar el número real del mapa, no el tag
                val realNumber = numberMap[view] ?: view.text.toString()
                pinBuilder.append(realNumber)
            }
        }

        updatePinDisplay()
        onPinChanged?.invoke(pinBuilder.toString())

        // Si alcanzó 6 dígitos, notificar que está completo
        if (pinBuilder.length == 6) {
            onPinComplete?.invoke(pinBuilder.toString())
        }
    }

    private fun updatePinDisplay() {
        val pinText = "● ".repeat(pinBuilder.length)
        pinDisplay.text = pinText.trim()
    }

    /**
     * Obtener el PIN actual
     */
    fun getPin(): String = pinBuilder.toString()

    /**
     * Limpiar el PIN
     */
    fun clearPin() {
        pinBuilder.clear()
        updatePinDisplay()
        onPinChanged?.invoke("")
    }

    /**
     * Reintentar (cambiar números de posición)
     */
    fun reshuffleNumbers() {
        numberMap.clear()
        shuffleNumbers()
        clearPin()
    }

    /**
     * Mostrar/ocultar el display
     */
    fun showDisplay(visible: Boolean) {
        pinDisplay.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
    }
}
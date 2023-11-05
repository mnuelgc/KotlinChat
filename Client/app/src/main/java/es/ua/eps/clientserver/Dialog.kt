package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class Dialog : ConstraintLayout {

    var text : TextView? = null

    constructor(ctx: Context) : super(ctx) {initialize()}
    constructor(ctx: Context, atts: AttributeSet?)
            : super(ctx, atts) {initialize()}
    constructor(ctx: Context, attrs: AttributeSet?, defStyle: Int)
            : super(ctx, attrs, defStyle) {initialize()}
    fun initialize(){    // Creamos la interfaz a partir del layout
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        li.inflate(R.layout.dialog, this, true)

        // Obtenemos las referencias a las vistas hijas
        text = findViewById<TextView>(R.id.textDialog)
    }

    fun setText(newText : String)
    {
        text?.text = newText
    }
}
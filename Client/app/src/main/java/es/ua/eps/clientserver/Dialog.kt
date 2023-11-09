package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import org.w3c.dom.Text

class Dialog : ConstraintLayout {

    var userName : TextView? = null
    var text : TextView? = null
    var linearLayout : LinearLayout? = null


    constructor(ctx: Context) : super(ctx) {initialize()}
    constructor(ctx: Context, atts: AttributeSet?)
            : super(ctx, atts) {initialize()}
    constructor(ctx: Context, attrs: AttributeSet?, defStyle: Int)
            : super(ctx, attrs, defStyle) {initialize()}
    fun initialize(){    // Creamos la interfaz a partir del layout
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        li.inflate(R.layout.dialog, this, true)

        // Obtenemos las referencias a las vistas hijas
        userName = findViewById(R.id.userName)
        text = findViewById<TextView>(R.id.textDialog)
        linearLayout = findViewById<LinearLayout>(R.id.dialog_layout)
    }

    fun setText(newText : String)
    {
        text?.text = newText
    }

    fun setName(newName : String)
    {
        userName?.text = newName
    }

    fun changeToRed()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_red)
    }
    fun changeToBlue()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_blue)
    }

    fun changeToGreen() {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_green)
        userName?.visibility = INVISIBLE
    }
    fun changeToPink()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_pink)
    }
    fun changeToYellow()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_yellow)
    }
    fun changeToCyan()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_cyan)
    }
}
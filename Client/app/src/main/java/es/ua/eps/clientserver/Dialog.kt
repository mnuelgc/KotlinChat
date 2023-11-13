package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

// Dialog encapsula la interfaz visual de un mensaje en la aplicación.
// Permite establecer el nombre del usuario,
// el contenido del mensaje y cambiar el color de fondo del mensaje según la categoría o tipo de mensaje.

// La clase Dialog extiende ConstraintLayout y representa un componente visual
// utilizado para mostrar mensajes en la interfaz de usuario.
class Dialog : ConstraintLayout {

    //    userName: Representa el nombre del usuario que envió el mensaje.
    //    text: Representa el contenido del mensaje.
    //    linearLayout: Representa el diseño lineal que contiene las vistas anteriores.

    var userName : TextView? = null
    var text : TextView? = null
    var linearLayout : LinearLayout? = null


    //  Tres constructores para la clase Dialog que llaman al método initialize() para inicializar la interfaz.
    constructor(ctx: Context) : super(ctx) {initialize()}
    constructor(ctx: Context, atts: AttributeSet?)
            : super(ctx, atts) {initialize()}
    constructor(ctx: Context, attrs: AttributeSet?, defStyle: Int)
            : super(ctx, attrs, defStyle) {initialize()}

    // initialize()
    //    El método initialize infla la interfaz de usuario a partir del archivo de diseño dialog.xml.
    //    Obtiene referencias a las vistas hijas por sus identificadores.

    fun initialize(){    // Creamos la interfaz a partir del layout
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        li.inflate(R.layout.dialog, this, true)

        // Obtenemos las referencias a las vistas hijas
        userName = findViewById(R.id.userName)
        text = findViewById<TextView>(R.id.textDialog)
        linearLayout = findViewById<LinearLayout>(R.id.dialog_layout)
    }

    // setText(newText : String)
    //    El método setText establece el texto del mensaje.
    fun setText(newText : String)
    {
        text?.text = newText
    }

    // setName(newName : String)
    //    El método setName establece el nombre del usuario.
    fun setName(newName : String)
    {
        userName?.text = newName
    }

    // Estos métodos cambian el fondo del linearLayout según el color especificado, utilizando recursos de fondo predefinidos.
    fun changeToRed()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_red)
    }
    fun changeToBeige()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_beige)
    }

    fun changeToOrange() {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_orange)
        userName?.visibility = INVISIBLE
    }
    fun changeToBrown()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_brown)
    }
    fun changeToQuasiBeig()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_quasibeig)
    }
    fun changeToQuasiWhite()
    {
        linearLayout?.setBackgroundResource(R.drawable.round_border_frame_quasiwhite)
    }
}
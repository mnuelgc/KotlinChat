package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.TOP

// la clase ChatSpaceView proporciona una vista desplazable que puede contener y mostrar dinámicamente diálogos (instancias de la clase Dialog).
// La función appendDialog facilita la adición de nuevos diálogos con ciertas propiedades de diseño.
// La función privada createDialog se encarga de la creación y configuración básica de un nuevo diálogo.

//    Se define la clase ChatSpaceView que hereda de ScrollView,
//    Este elemento compuesto se usará para que en la pantalla del chat sea facil añadir dialogos y hacer scroll.
//    Se declaran las variables constraintLayout, numsOfDialog (número de diálogos) y lastId (último ID de diálogo utilizado).
//    Se definen tres constructores para la clase, donde initialize() se llama para configurar la vista.
class ChatSpaceView : ScrollView {

    var constraintLayout : ConstraintLayout? = null
    var numsOfDialog : Int = 0
    var lastId : Int = 0

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int)
            : super(context, attrs, defStyle) {initialize()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {initialize()}
    constructor(context: Context?) : super(context) {initialize()}

    fun initialize(){
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.chat_space_view, this, true)
        constraintLayout = findViewById(R.id.messageBlackBoard)
    }


    // appendDialog:
    //    Esta función agrega un nuevo diálogo a la vista.
    //    Se crea un nuevo Dialog llamando a createDialog y luego se agrega al constraintLayout.
    //    Se utiliza ConstraintSet para establecer las restricciones del diálogo en el constraintLayout.
    //    Se ajusta la posición del diálogo según el parámetro from (0 para cliente, 1 para servidor).
    //    Se ajusta la posición vertical del diálogo en relación con el último diálogo agregado.
    //    Se establece el color del diálogo según el parámetro color.
    //    Se establece el nombre del usuario en el diálogo si está presente.
    //    Se aplican las restricciones al constraintLayout.

    public fun appendDialog(message : String, from: Int, color : Int, userName : String?){
        val dialog = createDialog(message, from)

        constraintLayout?.addView(dialog)

        val set = ConstraintSet()
        set.clone(constraintLayout)
        dialog.id.also { diag ->
            if(from == 1) {
                set.connect(diag, START, PARENT_ID, START)
            }else if (from == 0 ){
                set.connect(diag, END, PARENT_ID, END)
            }
            if(lastId == 0){
                set.connect(diag, TOP, PARENT_ID, TOP)
            }else{
                set.connect(diag, TOP, lastId, BOTTOM)
            }
            lastId = diag
        }

        when(color)
        {
            0 -> dialog.changeToRed()
            1 -> dialog.changeToQuasiWhite()
            2 -> dialog.changeToQuasiBeig()
            3 -> dialog.changeToBrown()
            4 -> dialog.changeToBeige()
            5 -> dialog.changeToOrange()
        }
        if (userName != null)
        {
            dialog.setName(userName)
        }
        set.applyTo(constraintLayout)

    }


    //createDialog:
    //    Esta función privada crea un nuevo Dialog.
    //    Incrementa numsOfDialog y asigna el nuevo ID al diálogo.
    //    Establece el texto del diálogo con el mensaje proporcionado.

    private fun createDialog(message: String, from : Int) : Dialog
    {
        val dialog = Dialog(this.context)
        numsOfDialog++
        dialog.id = numsOfDialog

        dialog.setText(message)


        return dialog
    }
}
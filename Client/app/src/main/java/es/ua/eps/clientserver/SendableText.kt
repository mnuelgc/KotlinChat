package es.ua.eps.clientserver

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// SendableText proporciona una interfaz para que el usuario ingrese mensajes,
// Los envíe al servidor al hacer clic en un botón y limpie el cuadro de texto después de enviar el mensaje.
class SendableText : LinearLayout {

    var text : EditText? = null
    var button : Button? = null
    constructor(ctx: Context?) : super(ctx) {initialize()}
    constructor(ctx: Context?, atts: AttributeSet?)
            : super(ctx, atts) {initialize()}
    constructor(ctx: Context, attrs: AttributeSet?, defStyle: Int)
            : super(ctx, attrs, defStyle) {initialize()}

    // Este método se llama desde los constructores y realiza las siguientes acciones:
    //
    //    Infla la interfaz de usuario a partir del diseño sendable_text.
    //    Obtiene referencias a las vistas hijas (EditText y Button).
    //    Configura un OnClickListener para el botón.
    //    Dentro del OnClickListener, lanza una tarea en un hilo de fondo (Dispatchers.IO) utilizando GlobalScope.launch.
    //    En este hilo, se utiliza SystemClient.sendMessageToServer para enviar el contenido del cuadro de texto al servidor.
    //    Después de enviar el mensaje, se utiliza withContext(Dispatchers.Main) para actualizar la interfaz de usuario en el hilo principal,
    //    estableciendo el contenido del cuadro de texto a una cadena vacía.
    private fun initialize(){
        // Creamos la interfaz a partir del layout
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        li.inflate(R.layout.sendable_text, this, true)

        // Obtenemos las referencias a las vistas hijas
        text = findViewById<EditText>(R.id.editText)
        button = findViewById<Button>(R.id.button)

        button!!.setOnClickListener{
            GlobalScope.launch(Dispatchers.IO) {
                SystemClient.sendMessageToServer(text!!.text.toString())
                withContext(Dispatchers.Main) {
                    text!!.setText("")
                }
            }
        }
    }
}

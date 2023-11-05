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

class SendableText : LinearLayout {

    var text : EditText? = null
    var button : Button? = null
    constructor(ctx: Context?) : super(ctx) {initialize()}
    constructor(ctx: Context?, atts: AttributeSet?)
            : super(ctx, atts) {initialize()}
    constructor(ctx: Context, attrs: AttributeSet?, defStyle: Int)
            : super(ctx, attrs, defStyle) {initialize()}

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

/*package es.ua.eps.clientserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import es.ua.eps.clientserver.databinding.ActivityProbesBinding

class ProbesActivity : AppCompatActivity() {
    lateinit var viewBinding : ActivityProbesBinding
    lateinit var chatSpaceView: ChatSpaceView
    lateinit var buttonProbes : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var numeroDialogo = 0
        viewBinding = ActivityProbesBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        chatSpaceView = viewBinding.chatSpace
        buttonProbes = viewBinding.buttonProbe

        buttonProbes.setOnClickListener{
            numeroDialogo++
            chatSpaceView.appendDialog("DIALOGO NUMERO $numeroDialogo")
        }
    }
}*/
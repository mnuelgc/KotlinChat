package es.ua.eps.clientserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var viewBinding : ActivityMainBinding

    lateinit var response : TextView

    lateinit var editTextAddress: EditText
    lateinit var editTextPort: EditText

    lateinit var buttonConnect : Button
    lateinit var buttonDisconnet : Button

    lateinit var messageText : EditText
    lateinit var buttonSendMessage : Button

    lateinit var myClient :Client
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        response = viewBinding.ServerResponse

        editTextAddress = viewBinding.editTextAdress
        editTextPort = viewBinding.editTextPort

        buttonConnect = viewBinding.buttonConnect
        buttonDisconnet = viewBinding.buttonDisconnect

        messageText = viewBinding.messageText
        buttonSendMessage = viewBinding.sendMessageButton

        editTextAddress.setText("192.168.1.46")
        editTextPort.setText("8080")

        myClient = Client(response, viewBinding)


        buttonConnect.setOnClickListener{
            if (editTextAddress.text.toString() !="" && editTextPort.text.toString() != "")
            {
                myClient.setAddress(editTextAddress.text.toString())
                myClient.setPort(editTextPort.text.toString().toInt())
                lifecycleScope.launch(Dispatchers.IO){
                    myClient.connectClientToServer()

                    withContext(Dispatchers.Main) {
                        myClient.writeResponse()
                    }
                }
            }
        }

        buttonDisconnet.setOnClickListener{
            myClient.closeComunication()
            myClient.writeResponse()
        }

        buttonSendMessage.setOnClickListener{
            lifecycleScope.launch(Dispatchers.IO) {
                myClient.sendMessageToServer(" CHACHO QUE FUNCIONA")
            }
        }
    }
}
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

class MainActivity : AppCompatActivity() {
    lateinit var viewBinding : ActivityMainBinding

    lateinit var response : TextView

    lateinit var editTextAddress: EditText
    lateinit var editTextPort: EditText

    lateinit var buttonConnect : Button
    lateinit var buttonClear : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        response = viewBinding.ServerResponse

        editTextAddress = viewBinding.editTextAdress
        editTextPort = viewBinding.editTextPort

        buttonConnect = viewBinding.buttonConnect
        buttonClear = viewBinding.buttonClear

        buttonConnect.setOnClickListener{
            val myClient = Client(editTextAddress.text.toString(), editTextPort.text    .toString().toInt(), response)

            lifecycleScope.launch(Dispatchers.IO){
                myClient.connectClientToServer()

                withContext(Dispatchers.Main){
                    myClient.writeResponse()
                }
            }
        }

        buttonClear.setOnClickListener{
            response.text =""
        }
    }
}
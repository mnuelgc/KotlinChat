package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityMainBinding

    lateinit var response: TextView

    lateinit var editTextAddress: EditText
    lateinit var editTextPort: EditText

    lateinit var buttonConnect: Button
    lateinit var buttonDisconnet: Button


    lateinit var myClient: Client
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        response = viewBinding.ServerResponse

        editTextAddress = viewBinding.editTextAdress
        editTextPort = viewBinding.editTextPort

        buttonConnect = viewBinding.buttonConnect
        buttonDisconnet = viewBinding.buttonDisconnect

        editTextAddress.setText("192.168.1.46")
        editTextPort.setText("8080")

        //editTextAddress.setText("172.20.10.5")
        //editTextPort.setText("8080")
        myClient = Client()

        buttonConnect.setOnClickListener {
            if (editTextAddress.text.toString() != "" && editTextPort.text.toString() != "") {
                myClient.setAddress(editTextAddress.text.toString())
                myClient.setPort(editTextPort.text.toString().toInt())

                SystemClient.setClient(myClient)
                GlobalScope.launch(Dispatchers.IO) {
                    SystemClient.connectClientToServer()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    while (true) {
                        if (SystemClient.isclientConected()) {
                            GlobalScope.launch(Dispatchers.Main) {

                                val intentOpenChat =
                                    Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(intentOpenChat)
                            }
                            break
                        }
                    }
                }
            }
        }

        buttonDisconnet.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                SystemClient.closeComunication()
                SystemClient.writeResponse(viewBinding.root)
            }
        }


    }
}
package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityConversationBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity() {


    lateinit var viewBinding: ActivityConversationBinding

    lateinit var buttonDisconnet: Button
    lateinit var chatSpaceView: ChatSpaceView

    companion object {
        const val  EXTRA_CLIENT  = "Extra_Client"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        chatSpaceView = viewBinding.chatSpace
        buttonDisconnet = viewBinding.buttonDisconnect

        SystemClient.setRootView(viewBinding.root)

        buttonDisconnet.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                SystemClient.goOutChatRoom()
                finish()
            }
        }
    }
}
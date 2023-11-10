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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationActivity : AppCompatActivity() {


    lateinit var viewBinding: ActivityConversationBinding

    lateinit var buttonDisconnet: Button
    lateinit var chatSpaceView: ChatSpaceView

    var askChatRoomsCorroutine: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        chatSpaceView = viewBinding.chatSpace
        buttonDisconnet = viewBinding.buttonDisconnect

        SystemClient.setRootView(viewBinding.root)

        buttonDisconnet.setOnClickListener {
                askChatRoomsCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                    SystemClient.askForChatRoomList()
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    SystemClient.setRootView(viewBinding.root)
                    askChatRoomsCorroutine!!.join()
                    withContext(Dispatchers.IO) {
                        while (SystemChatRoomList.mutableMap.count() == 0) {
                        }
                    }
                    SystemClient.goOutChatRoom()
                    finish()
                }
        }
    }
}
package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityHomeBinding

    lateinit var buttonCreate: Button
    lateinit var buttonJoin: Button
    lateinit var buttonDisconnet: Button


    var askChatRoomsCorroutine: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        buttonCreate = viewBinding.buttonCreate
        buttonJoin = viewBinding.buttonJoin
        buttonDisconnet = viewBinding.buttonDisconnect


        buttonCreate.setOnClickListener {
            val intentCreateChatRoom = Intent(this@HomeActivity, CreateChatRoomActivity::class.java)
            startActivity(intentCreateChatRoom)
        }

        buttonJoin.setOnClickListener {
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

                val intentOpenChat =
                    Intent(this@HomeActivity, ChatRoomListActivity::class.java)
                startActivity(intentOpenChat)
            }
        }
        buttonDisconnet.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                SystemClient.closeComunication()
                finish()
                //   SystemClient.writeResponse(null,viewBinding.root)
            }
        }
        /*
        var joined: Boolean
        joined = false
        joinChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
            joined = SystemClient.joinChatRoom()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            joinChatCorroutine!!.join()
            if (joined) {
                val intentOpenChat =
                    Intent(this@HomeActivity, ConversationActivity::class.java)
                startActivity(intentOpenChat)
            }
        }
        */
    }
}
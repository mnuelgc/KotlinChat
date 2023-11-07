package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityHomeBinding

    lateinit var buttonCreate: Button
    lateinit var buttonJoin: Button

    var createChatCorroutine: Job? = null
    var joinChatCorroutine: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        buttonCreate = viewBinding.buttonCreate
        buttonJoin = viewBinding.buttonJoin

        buttonCreate.setOnClickListener {
            createChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                SystemClient.createChatRoom()
            }

            lifecycleScope.launch(Dispatchers.Main) {
                createChatCorroutine!!.join()
                val intentOpenChat = Intent(this@HomeActivity, ConversationActivity::class.java)
                startActivity(intentOpenChat)
            }
        }


        buttonJoin.setOnClickListener {
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
        }
    }
}
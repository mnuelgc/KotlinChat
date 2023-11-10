package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityChatRoomListBinding
import es.ua.eps.clientserver.databinding.ActivityCreateChatRoomBinding
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateChatRoomActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityCreateChatRoomBinding

    lateinit var buttonCreateRoom: Button
    lateinit var buttonGoBack : Button

    lateinit var editTextRoomsName: EditText

    var createChatCorroutine: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityCreateChatRoomBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        buttonCreateRoom = viewBinding.buttonCreateRoom
        buttonGoBack = viewBinding.buttonBack

        editTextRoomsName = viewBinding.editTextRoomsName

        buttonCreateRoom.setOnClickListener {
            createChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                if (editTextRoomsName.text.toString() != "") {
                    SystemClient.createChatRoom(editTextRoomsName.text.toString())
                }
            }

            lifecycleScope.launch(Dispatchers.Main) {
                createChatCorroutine!!.join()
                val intentOpenChat = Intent(this@CreateChatRoomActivity, ConversationActivity::class.java)
                startActivity(intentOpenChat)
            }
        }

        buttonGoBack.setOnClickListener{
            finish()
        }

    }
}
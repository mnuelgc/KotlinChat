package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import es.ua.eps.clientserver.databinding.ActivityChatRoomListBinding
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatRoomListActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityChatRoomListBinding
    lateinit var buttonGoBack : Button


    var joinChatCorroutine : Job? = null

    lateinit var list: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityChatRoomListBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        list = viewBinding.list

        buttonGoBack = viewBinding.buttonBack

        val rooms = mutableListOf<String>()
        for (i in 0 ..<SystemChatRoomList.mutableMap.size) { // sumar 1
            rooms.add(SystemChatRoomList.mutableMap[i]!!)
        }

        val adapter = RoomsAdapter(
            this,
            R.layout.item_room, rooms
        )

        list.adapter = adapter


        list.setOnItemClickListener { parent: AdapterView<*>, view: View,
                                      position: Int, id: Long ->

            var joined: Boolean
            joined = false
            joinChatCorroutine = lifecycleScope.launch(Dispatchers.IO) {
                joined = SystemClient.joinChatRoom(position + 1)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                joinChatCorroutine!!.join()
                if (joined) {
                    val intentOpenChat =
                        Intent(this@ChatRoomListActivity, ConversationActivity::class.java)

                    SystemChatRoomList.mutableMap.clear()
                    startActivity(intentOpenChat)
                }
            }
        }

        buttonGoBack.setOnClickListener{
            SystemChatRoomList.mutableMap.clear()
            finish()
        }
    }
}
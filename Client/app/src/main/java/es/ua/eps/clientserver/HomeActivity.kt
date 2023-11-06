package es.ua.eps.clientserver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import es.ua.eps.clientserver.databinding.ActivityHomeBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityHomeBinding

    lateinit var buttonCreate: Button
    lateinit var buttonJoin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)

        buttonCreate = viewBinding.buttonCreate
        buttonJoin = viewBinding.buttonJoin

        buttonCreate.setOnClickListener {
            val intentOpenChat = Intent(this@HomeActivity, ConversationActivity::class.java)
            startActivity(intentOpenChat)
        }

        buttonJoin.setOnClickListener {
            val intentOpenChat = Intent(this@HomeActivity, ConversationActivity::class.java)
            startActivity(intentOpenChat)
        }
    }
}
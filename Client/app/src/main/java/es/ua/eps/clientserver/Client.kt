package es.ua.eps.clientserver

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import es.ua.eps.clientserver.databinding.ActivityConversationBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Serializable
import java.net.Socket
import java.net.UnknownHostException

const val DISCONNECT_CODE : Int = 1616
const val CREATE_CHAT_ROOM_CODE : Int = 2001
const val JOIN_CHAT_ROOM_CODE : Int = 2002
const val GO_OUT_CHAT_ROOM_CODE : Int = 2003

class Client() : Serializable {
    var dstAddress :String?=""
    var dsPort = 0

    var isConnectedToServer = false
    var response : String? = ""
    var socket: Socket? = null

    var responseText : Dialog? = null

    var parentView : View? = null

    var messagesInList = 0

    public fun setAddress(address :String){
        dstAddress = address
        dstAddress = address
    }

    public fun setPort(port :Int){
        dsPort = port
    }

    suspend fun connectClientToServer() {
        withContext(Dispatchers.IO) {
            if (!isConnectedToServer) {
                try {
                    if (socket == null) socket = Socket(dstAddress, dsPort)

                    val byteArrayOutputStream = ByteArrayOutputStream(1024)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    val inputStream = socket!!.getInputStream()

                    if (socket != null) {
                        response = ""
                        isConnectedToServer = true
                    }

                    while (true) { //Bucle de lectura de mensajes del server
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) {
                            // Se ha llegado al final del flujo de entrada
                            break
                        }

                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        response += byteArrayOutputStream.toString("UTF-8")

                        withContext(Dispatchers.Main){
                           if(parentView != null)
                           {
                                writeResponse(parentView!!)
                                response = ""
                               byteArrayOutputStream.reset()
                            }
                        }
                    }

                } catch (ex: UnknownHostException) {
                    ex.printStackTrace()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    suspend fun sendMessageToServer(message: String){
        if (isConnectedToServer) {
            val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)
            writer.println (message)
            writer.flush()
            withContext(Dispatchers.Main){
                val chatSpaceView = parentView?.findViewById<ChatSpaceView>(R.id.chatSpace)
                chatSpaceView?.appendDialog(message, 0)
            }
        }
    }

    suspend fun notifyServerClose()
    {
        sendMessageToServer(DISCONNECT_CODE.toString())
        isConnectedToServer = false
    }

    suspend fun createChatRoom(){
        val createRoomMessage = "${CREATE_CHAT_ROOM_CODE}SALA de fiesta"
        sendMessageToServer(createRoomMessage)
    }

    suspend fun joinChatRoom() : Boolean{
        val createRoomMessage = "${JOIN_CHAT_ROOM_CODE}"
        sendMessageToServer(createRoomMessage)
        return true
    }

    suspend fun writeResponse(rootView : View){
        withContext(Dispatchers.Main) {
            writeDialog(response, rootView)
        }
    }

    suspend fun closeComunication()
    {
        if (isConnectedToServer)
        {
            response = "Disconnected From Server"
            notifyServerClose()
            withContext(Dispatchers.IO) {
                socket?.close()
                socket = null
            }
        }
    }

    fun writeDialog(message: String?, rootView: View)
    {
        var chatSpaceView = rootView.findViewById<ChatSpaceView>(R.id.chatSpace)
        chatSpaceView.appendDialog(message!!, 1)
    }


    fun setRootView(newRootView : View) {
        parentView = newRootView
    }
}
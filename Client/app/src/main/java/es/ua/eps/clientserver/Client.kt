package es.ua.eps.clientserver

import android.content.Context
import android.graphics.Color
import android.os.Message
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import es.ua.eps.clientserver.databinding.ActivityConversationBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import kotlin.math.absoluteValue

const val CONNECT_CODE : Int = 1515
const val DISCONNECT_CODE: Int = 1616
const val CREATE_CHAT_ROOM_CODE: Int = 2001
const val JOIN_CHAT_ROOM_CODE: Int = 2002
const val GO_OUT_CHAT_ROOM_CODE: Int = 2003
const val ASK_FOR_CHATS_ROOM_CODE: Int = 2004
const val RECIVE_CHATS_ROOM_CODE: Int = 2005

const val CLIENT_COMUNICATION_MESSAGE_CODE : Int = 3001


class Client() : Serializable {

    var name = ""
    var dstAddress: String? = ""
    var dsPort = 0

    var isConnectedToServer = false
    var response: String? = ""
    var socket: Socket? = null

    var responseText: Dialog? = null

    var parentView: View? = null

    var messagesInList = 0

    init{
        clientColor.add(Color.parseColor("#FF0000"))
        clientColor.add(Color.parseColor("#FFFF00"))
        clientColor.add(Color.parseColor("#FF00FF"))
        clientColor.add(Color.parseColor("#00FFFF"))
        clientColor.add(Color.parseColor("#F000F0"))
    }

    public fun setAddress(address: String) {
        dstAddress = address
    }

    public fun setPort(port: Int) {
        dsPort = port
    }

    suspend fun connectClientToServer(userName : String) {
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
                        sendMessageToServer("${CONNECT_CODE}${userName}")
                        name = userName
                    }

                    while (true) { //Bucle de lectura de mensajes del server
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) {
                            // Se ha llegado al final del flujo de entrada
                            break
                        }

                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        response += byteArrayOutputStream.toString("UTF-8")

                        // withContext(Dispatchers.IO) {
                        if (parentView != null) {
                            parseMessage(response!!)
                            response = ""
                            byteArrayOutputStream.reset()
                        }
                        //}
                    }

                } catch (ex: UnknownHostException) {
                    ex.printStackTrace()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    suspend fun sendMessageToServer(message: String) {
        if (isConnectedToServer) {
            if (message.startsWith(ASK_FOR_CHATS_ROOM_CODE.toString())
                || message.startsWith(CONNECT_CODE.toString())
                || message.startsWith(DISCONNECT_CODE.toString())
                || message.startsWith(CREATE_CHAT_ROOM_CODE.toString())
                || message.startsWith(JOIN_CHAT_ROOM_CODE.toString())
                || message.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())
                || message.startsWith(RECIVE_CHATS_ROOM_CODE.toString())) {
                val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)
                writer.println(message)
                writer.flush()
            }
            else {
                val mesageToSend = "$CLIENT_COMUNICATION_MESSAGE_CODE$name~$message"
                val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)
                writer.println(mesageToSend)
                writer.flush()
                withContext(Dispatchers.Main) {
                    val chatSpaceView = parentView?.findViewById<ChatSpaceView>(R.id.chatSpace)
                    chatSpaceView?.appendDialog(message, 0,5, null)
                }
            }
        }
    }

    suspend fun notifyServerClose() {
        sendMessageToServer(DISCONNECT_CODE.toString())
        isConnectedToServer = false
    }

    suspend fun createChatRoom(roomName : String) {
        val createRoomMessage = "${CREATE_CHAT_ROOM_CODE}$roomName"
        sendMessageToServer(createRoomMessage)
    }

    suspend fun joinChatRoom(chatRoomId : Int): Boolean {
        val joinRoomMessage = "${JOIN_CHAT_ROOM_CODE}$chatRoomId"
        sendMessageToServer(joinRoomMessage)
        return true
    }

    suspend fun goOutChatRoom() {
        val quitRoomMessage = "${GO_OUT_CHAT_ROOM_CODE}"
        sendMessageToServer(quitRoomMessage)
    }

    suspend fun askForChatRoomList() {
        withContext(Dispatchers.IO) {
            val askForChatRoomList = "${ASK_FOR_CHATS_ROOM_CODE}"

            sendMessageToServer(askForChatRoomList)
        }
        // val askForChatRoomList = "${ASK_FOR_CHATS_ROOM_CODE}"
        //withContext(Dispatchers.IO){ waitResponseFromServer()}
        // withContext(Dispatchers.IO){sendMessageToServer(askForChatRoomList)}
        //delay(2000)
    }

    fun parseChatRooms(message: String){
        //2005~NUM_SALAS2~{ID_SALA1~NAME_SALANombre}{ID_SALA2~NAME_SALANombre}
        var mes = message.substringAfter("$RECIVE_CHATS_ROOM_CODE~")
        val numSalas = mes.substringAfter("NUM_SALAS")

        var fragments = numSalas.split("~")

        var num = fragments[0].toInt()
        for (i in 1..<fragments.count() step 2) {
            //  writeDialog(num.toString(), parentView!!)

            if (fragments[i] != "") {
                var idSala = fragments[i].substringAfter("{ID_SALA").toInt()
                var nameSala = fragments[i + 1].substringAfter("NAME_SALA")
                nameSala = nameSala.substringBefore("}")

                // writeDialog(idSala.toString(), parentView!!)
                //writeDialog(nameSala, parentView!!)

                SystemChatRoomList.addRoom(idSala, nameSala)
                //  writeDialog(fragments[i], parentView!!)
            }
        }
    }
    suspend fun parseMessage(message: String) {
        if (message.startsWith(RECIVE_CHATS_ROOM_CODE.toString())) {
            parseChatRooms(message)
        } else if (message.startsWith(CLIENT_COMUNICATION_MESSAGE_CODE.toString())) {
            val newMessage = message.removePrefix(CLIENT_COMUNICATION_MESSAGE_CODE.toString())
            writeResponse(newMessage, parentView!!)
        }
    }

    suspend fun writeResponse(message : String, rootView: View) {
        withContext(Dispatchers.Main) {
            writeDialog(message, rootView)
        }
    }

    suspend fun closeComunication() {
        if (isConnectedToServer) {
            response = "Disconnected From Server"
            notifyServerClose()
            withContext(Dispatchers.IO) {
                socket?.close()
                socket = null
            }
        }
    }

    suspend fun writeDialog(message: String?, rootView: View) {
        withContext(Dispatchers.Main) {
            var chatSpaceView = rootView.findViewById<ChatSpaceView>(R.id.chatSpace)
            var messagFragments = message?.split("~")
            val userName = messagFragments?.get(0)
            val color = messagFragments?.get(1)?.toInt()
            val newMessage = messagFragments?.get(2)
            chatSpaceView.appendDialog(newMessage!!, 1, color!!, userName)
        }
    }


    fun setRootView(newRootView: View) {
        parentView = newRootView
    }

    companion object{
        var clientColor = mutableListOf <Int>()

    }
}
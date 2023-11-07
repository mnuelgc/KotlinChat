package es.ua.eps.chatserver

import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

const val DISCONNECT_CODE : Int = 1616
const val CREATE_CHAT_ROOM_CODE : Int = 2001
const val JOIN_CHAT_ROOM_CODE : Int = 2002
const val GO_OUT_CHAT_ROOM_CODE : Int = 2003

class Server internal constructor(
    val serverIp_text : TextView,
    val serverInfo_text : TextView,
    val serverPort_text : TextView
) {

    val SocketServerPORT = 8080
    var serverRunning = false


    var count = 0
    var message : String? = ""

    var salasDeChat = mutableMapOf<Int, ChatRoom>()

    var lobbyRoom = ChatRoom(0, "Lobby")

        private lateinit var serverSocket: ServerSocket
    public var clients : ArrayList <Socket> = ArrayList<Socket>()

    public suspend fun initSocket() {
        if (!serverRunning) {
            try {
                withContext(Dispatchers.IO) {
                    serverSocket =
                        ServerSocket(SocketServerPORT)

                    salasDeChat.put(lobbyRoom.getId(),lobbyRoom)
                    withContext(Dispatchers.Main) {
                        serverIp_text.text = getIpAddress()

                        serverPort_text.text = ("I'm waiting here: "
                                + serverSocket.localPort)
                        //  serverInfo_text.text = ""
                    }
                    serverRunning = true
                    count = 0
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    suspend fun waitConnection() {
        try{
            val socket = serverSocket!!.accept()

            count++
            message += """#$count from ${socket.inetAddress}:${socket.port}"""


            withContext(Dispatchers.Main) {
                serverInfo_text.text = message
            }


            val lobbyLocal = salasDeChat.get(0)
            lobbyLocal!!.clientGetIn(ClientInServer(count, socket))

            salasDeChat.put(0, lobbyLocal)

            message += "\nHAY ${lobbyLocal?.howManyClients()} clientes\n"
            message += "HAY ${salasDeChat.count()} salas\n"


            socketServerReply(socket, null, 0, null)
            count++

            withContext(Dispatchers.Main) {
                serverInfo_text.text = message
            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun socketServerReply(hostThreadSocket: Socket, salaActual: ChatRoom?, cnt: Int, respon : String?){
        var outputStream: OutputStream
        var msgReply = ""
        if(respon != null) msgReply = "$respon"
        else  msgReply = "Estás en la sala $cnt \n"
        try {
            if (salaActual != null) {
                for (client in salaActual.getClients()) {
                    val socket = client.getSocket()
                    if (socket != hostThreadSocket)
                    {
                        outputStream = socket.getOutputStream()
                        val printWriter = PrintWriter(outputStream)
                        printWriter.write(msgReply)
                        printWriter.flush()

                        message += "replayed: $msgReply\n"
                    }
                }
            }else{
              /*  outputStream = hostThreadSocket.getOutputStream()
                val printWriter = PrintWriter(outputStream)
                printWriter.write(msgReply)
                printWriter.flush()

                message += "replayed: $msgReply"

               */
            }


        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            message += "Something wrong! $e\n"
        }

        withContext(Dispatchers.Main) {
            serverInfo_text.text = message

        }
    }
    suspend fun readMessages() {
        withContext(Dispatchers.IO) {
            for (i in 0 until salasDeChat.count())
            {
                var salaActual = salasDeChat[i]

                if (salaActual != null) {
                    for (client in salaActual.getClients()) {
                        val socket = client.getSocket()
                        val byteArrayOutputStream = ByteArrayOutputStream(1024)
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        val inputStream = socket.getInputStream()
                        var clientMessage = ""

                        GlobalScope.launch(Dispatchers.IO) {
                            while (true) {
                                println(inputStream.available())

                                bytesRead = inputStream.read(buffer)

                                if (bytesRead == -1) {
                                    break
                                }
                                byteArrayOutputStream.write(buffer, 0, bytesRead)
                                clientMessage += byteArrayOutputStream.toString("UTF-8")

                                if (clientMessage.last() == '\n') {
                                    clientMessage = clientMessage.dropLast(1)
                                }
                                parseClientMessage(clientMessage, socket, client, salaActual)
                                clientMessage = ""
                                byteArrayOutputStream.reset()
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun disconnectClient(clientSocket: Socket){
        withContext(Dispatchers.Main){
            message = "Client Log Out"
            count --
            serverInfo_text.text = message
        }
        clients.remove(clientSocket)
    }

    suspend fun parseClientMessage(
        clientMessage: String,
        senderSocket: Socket,
        client : ClientInServer,
        salaActual: ChatRoom?
    ){
        if (clientMessage.startsWith(DISCONNECT_CODE.toString())){
            disconnectClient(senderSocket)
        }
        else if (clientMessage.startsWith(CREATE_CHAT_ROOM_CODE.toString())) {
            createNewChatRoom(clientMessage, client)
        }
        else if (clientMessage.startsWith(JOIN_CHAT_ROOM_CODE.toString())) {
            joinToChatRoom(clientMessage, client)
        }
        else if (clientMessage.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())) {
        }
        else{
            socketServerReply(senderSocket, salaActual,  9999999, clientMessage)
        }
    }

    fun closeServer() {
        if (serverRunning) {
            try {

                serverRunning = false
                serverSocket.close()
                lobbyRoom.wipeRoom()
                salasDeChat.clear()
                serverPort_text.text = "Server Closed"
                serverIp_text.text = "Server Closed"
                serverInfo_text.text = ""
                message = ""
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun createNewChatRoom(clientMessage : String, client : ClientInServer)
    {
        val idSala = salasDeChat.count()
        val salaActual = ChatRoom(idSala, clientMessage.removePrefix(CREATE_CHAT_ROOM_CODE.toString()), client)
        salasDeChat.put(salaActual.getId(), salaActual)

        message += "Chat room created\n"
        message += "HAY ${salasDeChat.count()} salas\n"
        withContext(Dispatchers.Main) {
            serverInfo_text.text = message
        }
    }

    private suspend fun joinToChatRoom(clientMessage: String, client : ClientInServer){
        val salaActual = salasDeChat.get(1)
        salaActual?.clientGetIn(client)
        message += "Client join to room"
        message += "HAY ${salaActual?.howManyClients()} clientes\n"

        withContext(Dispatchers.Main) {
            serverInfo_text.text = message
        }

    }

    private fun getIpAddress() : String {
        var ip = ""

        try{
            val enumNetworkInterface = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterface.hasMoreElements()){
                val networkInterface = enumNetworkInterface.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements()){
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip += "SiteLocalAddress: " + inetAddress.hostAddress + "\n"
                    }
                }
            }

        }catch(se : SocketException){
            se.printStackTrace()
            ip += "Something Wrong! " + se.toString() + "\n"
        }

        return ip
    }
}
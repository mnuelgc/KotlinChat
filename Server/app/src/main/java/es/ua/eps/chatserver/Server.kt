package es.ua.eps.chatserver

import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

const val DISCONNECT_CODE: Int = 1616
const val CREATE_CHAT_ROOM_CODE: Int = 2001
const val JOIN_CHAT_ROOM_CODE: Int = 2002
const val GO_OUT_CHAT_ROOM_CODE: Int = 2003
const val ASK_FOR_CHATS_ROOM_CODE : Int = 2004
const val GIVE_CHATS_ROOM_CODE : Int = 2005

class Server internal constructor(
    val serverIp_text: TextView,
    val serverInfo_text: TextView,
    val serverPort_text: TextView
) {

    val SocketServerPORT = 8080
    var serverRunning = false


    var count = 0
    var message: String? = ""

    var salasDeChat = mutableMapOf<Int, ChatRoom>()
    var lobbyRoom = ChatRoom(0, "Lobby")

    private lateinit var serverSocket: ServerSocket
    public var clients: ArrayList<Socket> = ArrayList<Socket>()

    public suspend fun initSocket() {
        if (!serverRunning) {
            try {
                withContext(Dispatchers.IO) {
                    serverSocket =
                        ServerSocket(SocketServerPORT)

                    var lobbyRoom = ChatRoom(0, "Lobby")
                    salasDeChat[0] = lobbyRoom

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
        try {
            val socket = serverSocket!!.accept()

            count++
            message += """"#$count from ${socket.inetAddress}:${socket.port}"""


            withContext(Dispatchers.Main) {
                serverInfo_text.text = message
            }

            val client = ClientInServer(count, socket)
            /*
                        var salaActual :ChatRoom? = null
                        if (salasDeChat.count() == 0)
                        {
                            salaActual = ChatRoom(0, "Sala de Pepe", client)
                            salasDeChat[salaActual.getId()] = salaActual
                        }
                        else if(salasDeChat.count() == 1) {
                            if (salasDeChat.get(0)?.getClients()?.count() == 1) {
                                salaActual = salasDeChat.get(0)
                                salaActual?.clientGetIn(client)

                            }else{
                                salaActual = ChatRoom(1, "Sala de Juan", client)
                                salasDeChat[salaActual.getId()] = salaActual
                            }
                        }
                        else if(salasDeChat.count() == 2) {
                            salaActual = salasDeChat.get(1)
                            salaActual?.clientGetIn(client)
                        }
            */
            lobbyRoom.clientGetIn(client)
            salasDeChat[0] = lobbyRoom

            message += "HAY ${lobbyRoom.howManyClients()} clientes"

            socketServerReply(client, 0, null)

            withContext(Dispatchers.Main) {
                message += clients.count().toString() + "\n"
                serverInfo_text.text = message

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun socketServerReply(
        client: ClientInServer,
        cnt: Int,
        respon: String?
    ) {
        var salaActual = client.actualRoom
        var outputStream: OutputStream
        var msgReply = ""
        if (respon != null) msgReply = "$respon"
    //    else msgReply = "Estás en la sala $cnt \n"
        try {
            if (respon?.startsWith(GIVE_CHATS_ROOM_CODE.toString())== true)
            {
                val socket = client.getSocket()
                outputStream = socket.getOutputStream()
                val printWriter = PrintWriter(outputStream)
                printWriter.write(msgReply)
                printWriter.flush()

                message += "replayed: $msgReply\n"
            }
            else {
                if (salaActual != null) {
                    for (cliente in salaActual.getClients()) {
                        val socket = cliente.getSocket()
                        if (cliente.getSocket() != client.getSocket()) {
                            outputStream = socket.getOutputStream()
                            val printWriter = PrintWriter(outputStream)
                            printWriter.write(msgReply)
                            printWriter.flush()

                            message += "replayed: $msgReply\n"
                        }
                    }
                } else {
                    /*  outputStream = hostThreadSocket.getOutputStream()
                  val printWriter = PrintWriter(outputStream)
                  printWriter.write(msgReply)
                  printWriter.flush()

                  message += "replayed: $msgReply"

                 */
                }
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
            for (i in 0 until salasDeChat.count()) {
                var salaActual = salasDeChat[i]

                if (salaActual != null) {
                    for (client in salaActual.getClients()) {
                        processMessage(client, salaActual)
                    }
                }
            }
        }
    }

    suspend fun processMessage(client: ClientInServer, salaActual: ChatRoom?) {
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
                }
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                clientMessage += byteArrayOutputStream.toString("UTF-8")

                if (clientMessage.last() == '\n') {
                    clientMessage = clientMessage.dropLast(1)
                }
                parseClientMessage(clientMessage, client)
                clientMessage = ""
                byteArrayOutputStream.reset()
            }
        }
    }

    suspend fun disconnectClient(client: ClientInServer) {
        withContext(Dispatchers.Main) {
            message = "Client Log Out"
            count--
            serverInfo_text.text = message
        }
//        clients.remove(clientSocket)
    }


    suspend fun parseClientMessage(
        clientMessage: String,
        client: ClientInServer) {
        if (clientMessage.startsWith(DISCONNECT_CODE.toString())) {
            disconnectClient(client)
        } else if (clientMessage.startsWith(CREATE_CHAT_ROOM_CODE.toString())) {
            createNewChatRoom(clientMessage, client)
        } else if (clientMessage.startsWith(JOIN_CHAT_ROOM_CODE.toString())) {
            joinToChatRoom(clientMessage, client)
        } else if (clientMessage.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())) {
            goOutChatRoom(clientMessage, client)
        } else if (clientMessage.startsWith(ASK_FOR_CHATS_ROOM_CODE.toString())) {
            giveChatRooms(clientMessage, client)
        } else {
            socketServerReply(client, 9999999, clientMessage)
        }
    }

    fun closeServer() {
        if (serverRunning) {
            try {

                serverRunning = false
                serverSocket.close()
                clients.clear()
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

    private suspend fun createNewChatRoom(clientMessage: String, client: ClientInServer) {
        lobbyRoom.clientGoOut(client)
        salasDeChat[0] = lobbyRoom
        val idSala = salasDeChat.count()
        val salaActual =
            ChatRoom(idSala, clientMessage.removePrefix(CREATE_CHAT_ROOM_CODE.toString()), client)
        salasDeChat[salaActual.getId()] = salaActual

        message += "Chat room ${salaActual.getName()} created\n"
        message += "HAY ${salasDeChat.count()} salas\n"
        withContext(Dispatchers.Main) {
            serverInfo_text.text = message
        }

    }

    private suspend fun joinToChatRoom(clientMessage: String, client: ClientInServer) {
        lobbyRoom.clientGoOut(client)
        salasDeChat[0] = lobbyRoom
        val salaActual = salasDeChat[salasDeChat.count() -1]
        salaActual?.clientGetIn(client)
        salasDeChat[salaActual?.getId()!!] = salaActual

        message += "Client join to room ${salaActual?.getName()}"
        message += "HAY ${salaActual?.howManyClients()} clientes\n"

        message += salaActual.dataToTextFormat() + "\n"
        withContext(Dispatchers.Main) {
            serverInfo_text.text = message + "\n"
            serverInfo_text.text = message
        }

    }

    private suspend fun goOutChatRoom(
        clientMessage: String,
        client: ClientInServer
    ) {
        val chatRoom = client.actualRoom
        chatRoom!!.clientGoOut(client)
        message += "Client left chatRoom: ${chatRoom.getName()}"
        message += "HAY ${chatRoom?.howManyClients()} clientes\n"
        lobbyRoom.clientGetIn(client)
        salasDeChat[0] = lobbyRoom


        withContext(Dispatchers.Main) {
            serverInfo_text.text = message
        }
    }

    private suspend fun giveChatRooms(
        clientMessage: String,
        client: ClientInServer
    ) {
        var chatsRoomList = "$GIVE_CHATS_ROOM_CODE~NUM_SALAS${salasDeChat.count() - 1}~"
        for(i in 0 until salasDeChat.count()){
            if (i != 0)
            {
                chatsRoomList+= "{"

                chatsRoomList += salasDeChat[i]?.minimaldataToTextFormat()
                chatsRoomList+= "}~"

            }
        }
        socketServerReply(client, 0, chatsRoomList)

    }



    private fun getIpAddress(): String {
        var ip = ""

        try {
            val enumNetworkInterface = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterface.hasMoreElements()) {
                val networkInterface = enumNetworkInterface.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()

                    if (inetAddress.isSiteLocalAddress) {
                        ip += "SiteLocalAddress: " + inetAddress.hostAddress + "\n"
                    }
                }
            }

        } catch (se: SocketException) {
            se.printStackTrace()
            ip += "Something Wrong! " + se.toString() + "\n"
        }

        return ip
    }
}
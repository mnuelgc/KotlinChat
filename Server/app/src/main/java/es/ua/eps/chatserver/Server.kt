package es.ua.eps.chatserver

import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.sql.Time

const val DISCONNECT_CODE: Int = 1616
const val CREATE_CHAT_ROOM_CODE: Int = 2001
const val JOIN_CHAT_ROOM_CODE: Int = 2002
const val GO_OUT_CHAT_ROOM_CODE: Int = 2003

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

                    salasDeChat.put(lobbyRoom.getId(), lobbyRoom)
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
        while (true) {
            try {
                val socket = serverSocket!!.accept()

                count++
                message += """#$count from ${socket.inetAddress}:${socket.port}"""


                withContext(Dispatchers.Main) {
                    serverInfo_text.text = message
                }

                var client = ClientInServer(count, socket)

                lobbyRoom.clientGetIn(client)

                message += "\nHAY ${salasDeChat[0]?.howManyClients()} clientes\n"
                message += "HAY ${salasDeChat.count()} salas\n"


                socketServerReply(client, null, 0, null)

                withContext(Dispatchers.Main) {
                    serverInfo_text.text = message
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun socketServerReply(
        clientSender: ClientInServer,
        salaActual: ChatRoom?,
        cnt: Int,
        respon: String?
    ) {
        var outputStream: OutputStream
        var msgReply = ""
        if (respon != null) msgReply = "$respon"
        else msgReply = "Estás en la sala $cnt \n"
        try {
            if (salaActual != null) {
                for (client in salaActual.getClients()) {
                    val socket = client.getSocket()
                    if (socket != clientSender.getSocket()) {
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


        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            message += "Something wrong! $e\n"
        }

        withContext(Dispatchers.Main) {
            serverInfo_text.text = message

        }
    }

    /* suspend fun readMessages() {
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

                             println(inputStream.available())

                         try {
                             withTimeout(500) {
                                 bytesRead = inputStream.read(buffer)
                             }
                         }catch (_ : TimeoutCancellationException){
                             continue
                         }
                         // if (bytesRead == -1) {
                             //    break
                             //}
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
 */
    /* suspend fun readMessages() {
         while (true) {
             for (i in 0 until salasDeChat.count()) {
                 val salaActual = salasDeChat[i]

                 if (salaActual != null) {
                     withContext(Dispatchers.IO) {
                     for (client in salaActual.getClients()) {
                  //       withTimeoutOrNull(500) {

                                 val byteArrayOutputStream = ByteArrayOutputStream(1024)
                                 val buffer = ByteArray(1024)
                                 var bytesRead: Int
                                 val inputStream = client.getInputStream()
                                 var clientMessage = ""

                                 //    while (true) {

                                 bytesRead = inputStream.read(buffer)


                                 if (bytesRead == -1) {
                                     continue
                                 }
                                 byteArrayOutputStream.write(buffer, 0, bytesRead)
                                 clientMessage += byteArrayOutputStream.toString("UTF-8")

                                 if (clientMessage.last() == '\n') {
                                     clientMessage = clientMessage.dropLast(1)
                                 }
                                 parseClientMessage(clientMessage, client, salaActual)
                                 clientMessage = ""
                                 byteArrayOutputStream.reset()
                            // }
                         }
                         //continue
                     }
                 }
             }
         }
     }
 //}

     */
    suspend fun readMessages() {
        withContext(Dispatchers.IO) {
            for (i in 0 until salasDeChat.count()) {
                var salaActual = salasDeChat[i]

                if (salaActual != null) {
                    for (client in salaActual.getClients()) {
                        val byteArrayOutputStream = ByteArrayOutputStream(1024)
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        val inputStream = client.getInputStream()
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
                                parseClientMessage(clientMessage, client, salaActual)
                                clientMessage = ""
                                byteArrayOutputStream.reset()
                            }
                        }
                    }
                }
            }
        }
    }


    suspend fun disconnectClient(clientSocket: ClientInServer) {
        withContext(Dispatchers.Main) {
            message = "Client Log Out"
            count--
            serverInfo_text.text = message
        }
        TODO()
        //  clients.remove(clientSocket)
    }

    suspend fun parseClientMessage(
        clientMessage: String,
        client: ClientInServer,
        salaActual: ChatRoom?
    ) {
        if (clientMessage.startsWith(DISCONNECT_CODE.toString())) {
            disconnectClient(client)
        } else if (clientMessage.startsWith(CREATE_CHAT_ROOM_CODE.toString())) {
            createNewChatRoom(clientMessage, client)
        } else if (clientMessage.startsWith(JOIN_CHAT_ROOM_CODE.toString())) {
            joinToChatRoom(clientMessage, client)
        } else if (clientMessage.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())) {
            goOutChatRoom(clientMessage, client, salaActual!!)
        } else {
            socketServerReply(client, salaActual, 9999999, clientMessage)
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

    private suspend fun createNewChatRoom(clientMessage: String, client: ClientInServer) {
        /*  lobbyRoom.clientGoOut(client)
          val idSala = salasDeChat.count()
          val salaActual =
              ChatRoom(1, clientMessage.removePrefix(CREATE_CHAT_ROOM_CODE.toString()), client)
          salasDeChat.put(salaActual.getId(), salaActual)

          message += "Chat room ${salaActual.getName()} created\n"
          message += "HAY ${salasDeChat.count()} salas\n"
          withContext(Dispatchers.Main) {
              serverInfo_text.text = message
          }

    */
    }

    private suspend fun joinToChatRoom(clientMessage: String, client: ClientInServer) {
        /*   lobbyRoom.clientGoOut(client)
           val salaActual = salasDeChat.get(1)
           salaActual?.clientGetIn(client)
           message += "Client join to room ${salaActual?.getName()}"
           message += "HAY ${salaActual?.howManyClients()} clientes\n"

           withContext(Dispatchers.Main) {
               serverInfo_text.text = message
           }

         */
    }

    private suspend fun goOutChatRoom(
        clientMessage: String,
        client: ClientInServer,
        chatRoom: ChatRoom
    ) {
        chatRoom.clientGoOut(client)
        message += "Client left chatRoom: ${chatRoom.getName()}"
        message += "HAY ${chatRoom?.howManyClients()} clientes\n"
        lobbyRoom.clientGetIn(client)


        withContext(Dispatchers.Main) {
            serverInfo_text.text = message
        }
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
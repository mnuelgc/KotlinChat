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


    private lateinit var serverSocket: ServerSocket
    public var clients : ArrayList <Socket> = ArrayList<Socket>()

    public suspend fun initSocket() {
        if (!serverRunning) {
            try {
                withContext(Dispatchers.IO) {
                    serverSocket =
                        ServerSocket(SocketServerPORT)

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
            message += """"#$count from ${socket.inetAddress}:${socket.port}"""


            withContext(Dispatchers.Main) {
                serverInfo_text.text = message
            }

            val client = ClientInServer(count, socket)

            if (salasDeChat.count() == 0)
            {
                val salaActual = ChatRoom(0, "Sala de Pepe", client)
                salasDeChat.put(salaActual.getId(), salaActual)
            }
            else{
                val salaActual = salasDeChat.get(0)
                salaActual?.clientGetIn(client)
            }
            val salaActual = salasDeChat.get(0)

            println("HAY ${salaActual?.howManyClients()} clientes")

            socketServerReply(socket, null, 0, null)
            count++

            withContext(Dispatchers.Main) {
                message += clients.count().toString() + "\n"
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
                                parseClientMessage(clientMessage, socket, salaActual)
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
        salaActual: ChatRoom?
    ){
        if (clientMessage.startsWith(DISCONNECT_CODE.toString())){
            disconnectClient(senderSocket)
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
                clients.clear()

                serverPort_text.text = "Server Closed"
                serverIp_text.text = "Server Closed"
                serverInfo_text.text = ""
                message = ""
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
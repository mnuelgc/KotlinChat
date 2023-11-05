package es.ua.eps.chatserver

import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class Server internal constructor(
    val serverIp_text : TextView,
    val serverInfo_text : TextView,
    val serverPort_text : TextView
) {

    val DISCONNECT_CODE : Int = 1616



    val SocketServerPORT = 8080
    var serverRunning = false


    var count = 0
    var message : String? = ""

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

            socketServerReply(socket, count, null)

            clients.add(socket)

            withContext(Dispatchers.Main) {
                message += clients.count().toString() + "\n"
                serverInfo_text.text = message

            }
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun socketServerReply(hostThreadSocket: Socket, cnt: Int, respon : String?){
        val outputStream: OutputStream
        var msgReply = ""
        if(respon != null) msgReply = "$respon"
        else  msgReply = "Hello from Pepe, you are #$cnt \n"
        try {
            outputStream = hostThreadSocket.getOutputStream()
            val printWriter = PrintWriter(outputStream)
            printWriter.write(msgReply)
            printWriter.flush()

            message += "replayed: $msgReply"

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
            for (socket in clients) {
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
                        parseClientMessage(clientMessage, socket)
                        clientMessage = ""
                        byteArrayOutputStream.reset()
                    }
                }
            }
        }
    }

    suspend fun disconnectClient(clientSocket: Socket){
        withContext(Dispatchers.Main){
            val mess = "Client Log Out"
            count --
            serverInfo_text.text = mess
        }
        clients.remove(clientSocket)
    }

    suspend fun parseClientMessage(clientMessage : String, senderSocket : Socket){
        if (clientMessage.startsWith(DISCONNECT_CODE.toString())){
            disconnectClient(senderSocket)
        }
        else{
            socketServerReply(senderSocket, 9999999, clientMessage)
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
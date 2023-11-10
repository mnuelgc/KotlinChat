package es.ua.eps.chatserver

//Importación de las bibliotecas necesarias.
import android.graphics.Color
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.math.BigInteger
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

//Definición de constantes que representan códigos para diferentes operaciones entre el Servidor y el cliente.
// Constantes referentes a las conexiones.
const val CONNECT_CODE : Int = 1515
const val DISCONNECT_CODE: Int = 1616

// Constantes referentes a las salas de crupo (200).
const val CREATE_CHAT_ROOM_CODE: Int = 2001
const val JOIN_CHAT_ROOM_CODE: Int = 2002
const val GO_OUT_CHAT_ROOM_CODE: Int = 2003
const val ASK_FOR_CHATS_ROOM_CODE : Int = 2004
const val GIVE_CHATS_ROOM_CODE : Int = 2005

// Constantes referentes a la comunicación (3000).
const val CLIENT_COMUNICATION_MESSAGE_CODE : Int = 3001

//Declaración de la clase Server con tres propiedades que son las instancias de TextView del layout xml que se ha dado.
class Server internal constructor(
    val serverIp_text: TextView,
    val serverInfo_text: TextView,
    val serverPort_text: TextView
) {

    //Declaración de dos variables miembro de la clase Server para indicar el puerto por el que va la conexión.
    ///Y si el server está en funcionamiento o no

    val SocketServerPORT = 8080
    var serverRunning = false

    //Declaración de dos variables miembro de la clase Server estas indica el numero de conexiones, que luego usaremos para dar ids a los clientes.
    //Y la otra se usará para el log de eventos del server
    var count = 0
    var message: String? = ""

    //Declaramos una coleccion de Salas de Chat y la sala principal que es "Lobby", la sala donde los usuarios entran tras iniciar la sesion
    var salasDeChat = mutableMapOf<Int, ChatRoom>()
    var lobbyRoom = ChatRoom(0, "Lobby")

    //Declaración de una variable miembro de la clase Server que representa el socket del servidor
    private lateinit var serverSocket: ServerSocket

    // initSocket()
    //    La función comprueba si el servidor ya está en ejecución (serverRunning).
    //    Dentro de un bloque withContext(Dispatchers.IO), se crea un ServerSocket en el puerto especificado (8080) y se configuran algunas propiedades iniciales.
    //    Se muestra la dirección IP y el puerto del servidor en las TextView correspondientes en la interfaz de usuario.
    public suspend fun initSocket() {
        if (!serverRunning) {
            try {
                withContext(Dispatchers.IO) {
                    serverSocket =
                        ServerSocket(SocketServerPORT)

                    var lobbyRoom = ChatRoom(0, "Lobby")
                    salasDeChat[0] = lobbyRoom

                    serverColors.add(Color.parseColor("#FF0000"))
                    serverColors.add(Color.parseColor("#FFFF00"))
                    serverColors.add(Color.parseColor("#FF00FF"))
                    serverColors.add(Color.parseColor("#00FFFF"))
                    serverColors.add(Color.parseColor("#F000F0"))

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

    // waitConnection()
    //
    //    La función espera activamente la conexión de un cliente utilizando serverSocket.accept().
    //    Se incrementa el contador de clientes conectados y se actualiza el texto en la serverInfo_text.
    //    Se crea un objeto ClientInServer representando al nuevo cliente.
    //    El nuevo cliente se agrega a la sala de espera (lobbyRoom).
    //    Se responde al cliente con información sobre la cantidad de clientes en la sala de espera.

    suspend fun waitConnection() {
        try {
            val socket = serverSocket!!.accept()

            count++
            message += """"#$count from ${socket.inetAddress}:${socket.port}"""


            withContext(Dispatchers.Main) {
                serverInfo_text.text = message
            }

            val client = ClientInServer(count, socket)

            lobbyRoom.clientGetIn(client)
            salasDeChat[0] = lobbyRoom

            message += "HAY ${lobbyRoom.howManyClients()} clientes"

            socketServerReply(client, null)

            withContext(Dispatchers.Main) {
                serverInfo_text.text = message

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // socketServerReply(client: ClientInServer, respon: String?)
    //
    //    En función de la respuesta (respon) y el tipo de mensaje, se envía una respuesta al cliente.
    //    Si la respuesta comienza con GIVE_CHATS_ROOM_CODE, se envía la lista de salas de chat al cliente.
    //    Si no, se envía la respuesta a todos los clientes en la sala actual del cliente que envió el mensaje, salvo al emisor.
    private suspend fun socketServerReply(
        client: ClientInServer,
        respon: String?
    ) {
        var salaActual = client.actualRoom
        var outputStream: OutputStream
        var msgReply = ""
        if (respon != null) msgReply = "$respon"
        try {
            if (respon?.startsWith(CONNECT_CODE.toString())== true) { }
            else if (respon?.startsWith(GIVE_CHATS_ROOM_CODE.toString())== true)
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
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            message += "Something wrong! $e\n"
        }
        withContext(Dispatchers.Main) {
            serverInfo_text.text = message

        }
    }

    // readMessages()
    //
    //    Utiliza un bucle para iterar sobre todas las salas de chat y procesar los mensajes de cada cliente en esas salas.

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

    // processMessage(client: ClientInServer, salaActual: ChatRoom?)
    //
    //    Procesa los mensajes de un cliente, obteniendo el mensaje del InputStream del cliente y llamando a parseClientMessage para analizar y procesar el mensaje.
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
                    break
                }
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                clientMessage += byteArrayOutputStream.toString("UTF-8")

                if (clientMessage.last() == '\n') {
                    clientMessage = clientMessage.dropLast(1)
                }
                parseClientMessage(client, clientMessage)
                clientMessage = ""
                byteArrayOutputStream.reset()
            }
        }
    }

    // connectClient(client: ClientInServer, clientMessage: String)
    //
    //    Conecta a un cliente asignándole un nombre basado en el mensaje del cliente.
    suspend fun connectClient(client : ClientInServer, clientMessage : String)
    {
        val clientName = clientMessage.substringAfter(CONNECT_CODE.toString())
        client.setName(clientName)
    }

    // disconnectClient(client: ClientInServer)
    //
    //    Desconecta a un cliente actualizando el texto en serverInfo_text.
    suspend fun disconnectClient(client: ClientInServer) {
        withContext(Dispatchers.Main) {
            message += "Client Log Out"
            count--
            serverInfo_text.text = message
            lobbyRoom.clientGoOut(client)
        }
    }

    // parseClientMessage(client: ClientInServer, clientMessage: String)
    //
    //    Analiza y procesa el mensaje del cliente llamando a funciones específicas según el tipo de mensaje.
    //    Puede ser una solicitud de conexión, desconexión, creación de sala, etc.
    suspend fun parseClientMessage(

        client: ClientInServer,
        clientMessage: String) {
        if (clientMessage.startsWith(CONNECT_CODE.toString())) {
            connectClient(client, clientMessage)
        }
            if (clientMessage.startsWith(DISCONNECT_CODE.toString())) {
            disconnectClient(client)
        } else if (clientMessage.startsWith(CREATE_CHAT_ROOM_CODE.toString())) {
            createNewChatRoom(client, clientMessage)
        } else if (clientMessage.startsWith(JOIN_CHAT_ROOM_CODE.toString())) {
            joinToChatRoom(client, clientMessage)
        } else if (clientMessage.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())) {
            goOutChatRoom(client, clientMessage)
        } else if (clientMessage.startsWith(ASK_FOR_CHATS_ROOM_CODE.toString())) {
            giveChatRooms(client, clientMessage)
        } else if (clientMessage.startsWith(CLIENT_COMUNICATION_MESSAGE_CODE.toString())){
            giveTheMessage(client, clientMessage)
        }
    }

    // closeServer()
    //
    //    Cierra el servidor cerrando el ServerSocket, limpiando las listas de clientes y salas, y actualizando las TextView en la interfaz de usuario.
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

    // createNewChatRoom(client: ClientInServer, clientMessage: String)
    //
    //    Crea una nueva sala de chat y despues de eso une al cliente que ha creado la sala a esta y lo desconecta al cliente de la sala de espera.
    private suspend fun createNewChatRoom(client: ClientInServer, clientMessage: String) {
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

    // joinToChatRoom(client: ClientInServer, clientMessage: String)
    //
    //    Permite a un cliente unirse a una sala de chat existente, desconectándolo de la sala de espera.
    private suspend fun joinToChatRoom(client: ClientInServer, clientMessage: String) {
        lobbyRoom.clientGoOut(client)
        salasDeChat[0] = lobbyRoom
        var roomId = clientMessage.substringAfter(JOIN_CHAT_ROOM_CODE.toString()).toInt()
        val salaActual = salasDeChat[roomId]
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

    // goOutChatRoom(client: ClientInServer, clientMessage: String)
    //
    //    Permite a un cliente salir de una sala de chat, volviendo a la sala de espera.
    private suspend fun goOutChatRoom(
        client: ClientInServer,
        clientMessage: String
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

    // giveChatRooms(client: ClientInServer, clientMessage: String)
    //
    //    Envia la lista de salas de chat al cliente.
    private suspend fun giveChatRooms(
        client: ClientInServer,
        clientMessage: String
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
        socketServerReply(client,  chatsRoomList)

    }

    // giveTheMessage(client: ClientInServer, clientMessage: String)
    //
    //    Procesa y reenvía mensajes de un cliente a todos los demás clientes en la misma sala.
    private suspend fun giveTheMessage(client: ClientInServer, clientMessage: String)
    {
        // MessageTipo
        // 3001Perico~¿Hola Como estás?
        val messageWithoutCode = clientMessage.removePrefix(CLIENT_COMUNICATION_MESSAGE_CODE.toString())
        val messageFragments = messageWithoutCode.split("~")
        val user = messageFragments[0]
        val message = messageFragments[1]
        val newMessage = "${CLIENT_COMUNICATION_MESSAGE_CODE}$user~${client.getColor()}~$message"
        socketServerReply(client, newMessage)

    }

// getIpAddress(): String
//
//    Obtiene y devuelve la dirección IP del servidor.
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

    // companion object serverColors
    //
    //    Almacena colores del servidor como una lista mutable.
    companion object{
        var serverColors = mutableListOf <Int>()

    }
}
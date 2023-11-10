package es.ua.eps.clientserver

import android.content.Context
import android.graphics.Color
import android.os.Message
import android.os.Parcel
import android.os.Parcelable
import android.security.keystore.KeyProperties
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import es.ua.eps.clientserver.databinding.ActivityConversationBinding
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
import java.lang.Exception
import java.net.Socket
import java.net.UnknownHostException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.absoluteValue

// Client implementa la lógica del cliente en un entorno de red, manejando la conexión con el servidor, la creación y gestión de salas de chat,
// el envío y recepción de mensajes, así como operaciones criptográficas para la seguridad de las comunicaciones.
// La implementación de corrutinas (suspend fun) se utiliza para realizar operaciones asíncronas sin bloquear el hilo principal.


// Se definen constantes que representan códigos para diferentes tipos de mensajes en la comunicación del cliente con el servidor.
const val CONNECT_CODE: Int = 1515
const val DISCONNECT_CODE: Int = 1616
const val CREATE_CHAT_ROOM_CODE: Int = 2001
const val JOIN_CHAT_ROOM_CODE: Int = 2002
const val GO_OUT_CHAT_ROOM_CODE: Int = 2003
const val ASK_FOR_CHATS_ROOM_CODE: Int = 2004
const val RECIVE_CHATS_ROOM_CODE: Int = 2005

const val CLIENT_COMUNICATION_MESSAGE_CODE: Int = 3001

//  Se define la clase Client.
//  Se declaran las propiedades de la clase Client.
//  Se inicializan algunas propiedades en el bloque init, incluida la inicialización de colores en clientColor.
class Client() {

    var name = ""
    var dstAddress: String? = ""
    var dsPort = 0

    var isConnectedToServer = false
    var response: String? = ""
    var socket: Socket? = null

    var responseText: Dialog? = null

    var parentView: View? = null

    var messagesInList = 0

    init {
        clientColor.add(Color.parseColor("#FF0000"))
        clientColor.add(Color.parseColor("#FFFF00"))
        clientColor.add(Color.parseColor("#FF00FF"))
        clientColor.add(Color.parseColor("#00FFFF"))
        clientColor.add(Color.parseColor("#F000F0"))
    }

    // setAddress(address: String) y setPort(port: Int)
    //      Estas funciones simplemente asignan la dirección y el puerto a las propiedades dstAddress y dsPort, respectivamente.
    public fun setAddress(address: String) {
        dstAddress = address
    }

    public fun setPort(port: Int) {
        dsPort = port
    }

    // connectClientToServer(userName: String)
    // Esta función establece la conexión del cliente al servidor.
    // Utiliza corrutinas y realiza operaciones de red en el hilo de fondo (IO).
    // Dentro de ella, crea un socket, envía un mensaje al servidor para establecer la conexión,
    // y luego entra en un bucle para recibir mensajes del servidor de manera continua.

    suspend fun connectClientToServer(userName: String) {
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

    // sendMessageToServer(message: String)
    //      Esta función envía mensajes al servidor.
    //      Utiliza corrutinas y realiza operaciones de red en el hilo de fondo (IO).
    //      Si el mensaje es de ciertos tipos específicos, lo envía directamente al servidor.
    //      Si es un mensaje de cliente a cliente, realiza algunas operaciones criptográficas y luego lo envía,
    //      Aunque por algunas complicaciones con el funcionamiento de la encriptacion en Kotlin y los String (Explicado en el README)
    //      de momento el mensaje se envia sin encriptar.

    suspend fun sendMessageToServer(message: String) {
        withContext(Dispatchers.IO) {
            if (isConnectedToServer) {
                if (message.startsWith(ASK_FOR_CHATS_ROOM_CODE.toString())
                    || message.startsWith(CONNECT_CODE.toString())
                    || message.startsWith(DISCONNECT_CODE.toString())
                    || message.startsWith(CREATE_CHAT_ROOM_CODE.toString())
                    || message.startsWith(JOIN_CHAT_ROOM_CODE.toString())
                    || message.startsWith(GO_OUT_CHAT_ROOM_CODE.toString())
                    || message.startsWith(RECIVE_CHATS_ROOM_CODE.toString())
                ) {
                    val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)
                    writer.println(message)
                    writer.flush()
                } else {
                    val messageCrypted = encryptMessage(message, "encriptadito")

                    //val mesageToSend = "$CLIENT_COMUNICATION_MESSAGE_CODE$name~$messageCrypted"
                    val mesageToSend = "$CLIENT_COMUNICATION_MESSAGE_CODE$name~$message"
                    val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)

                    writer.println(mesageToSend)
                    writer.flush()
                    withContext(Dispatchers.Main) {
                        val msgproba = decryptMessage(messageCrypted, "encriptadito")
                        val chatSpaceView = parentView?.findViewById<ChatSpaceView>(R.id.chatSpace)
                        chatSpaceView?.appendDialog(message, 0, 5, null)
                    }
                }
            }
        }
    }

    // notifyServerClose()
    //      Esta función notifica al servidor que el cliente se desconectará enviándole un mensaje con el código de desconexión.
    //      Luego, establece isConnectedToServer como falso.
    suspend fun notifyServerClose() {
        sendMessageToServer(DISCONNECT_CODE.toString())
        isConnectedToServer = false
    }

    // createChatRoom(roomName: String) y joinChatRoom(chatRoomId: Int): Boolean
    //      Estas funciones permiten al cliente crear y unirse a una sala de chat, respectivamente.
    //      Envían mensajes al servidor con códigos específicos.
    suspend fun createChatRoom(roomName: String) {
        val createRoomMessage = "${CREATE_CHAT_ROOM_CODE}$roomName"
        sendMessageToServer(createRoomMessage)
    }


    suspend fun joinChatRoom(chatRoomId: Int): Boolean {
        val joinRoomMessage = "${JOIN_CHAT_ROOM_CODE}$chatRoomId"
        sendMessageToServer(joinRoomMessage)
        return true
    }

    // goOutChatRoom()
    //      Esta función notifica al servidor que el cliente desea salir de la sala de chat actual.
    suspend fun goOutChatRoom() {
        val quitRoomMessage = "${GO_OUT_CHAT_ROOM_CODE}"
        sendMessageToServer(quitRoomMessage)
    }

    // askForChatRoomList()
    //      Esta función solicita al servidor la lista de salas de chat disponibles.
    suspend fun askForChatRoomList() {
        withContext(Dispatchers.IO) {
            val askForChatRoomList = "${ASK_FOR_CHATS_ROOM_CODE}"

            sendMessageToServer(askForChatRoomList)
        }
    }

    // parseChatRooms(message: String)
    //      Esta función analiza los mensajes relacionados con las salas de chat
    //      y actualiza una estructura de datos (SystemChatRoomList) con la información recibida.
    fun parseChatRooms(message: String) {
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

    // parseMessage(message: String) y writeResponse(message: String, rootView: View)
    //      parseMessage analiza mensajes en función de su código.
    //      Si es un mensaje de comunicación entre clientes,
    //      invoca writeResponse para escribir el mensaje en la interfaz de usuario.
    suspend fun parseMessage(message: String) {
        if (message.startsWith(RECIVE_CHATS_ROOM_CODE.toString())) {
            parseChatRooms(message)
        } else if (message.startsWith(CLIENT_COMUNICATION_MESSAGE_CODE.toString())) {
            val newMessage = message.removePrefix(CLIENT_COMUNICATION_MESSAGE_CODE.toString())
            writeResponse(newMessage, parentView!!)
        }
    }

    suspend fun writeResponse(message: String, rootView: View) {
        withContext(Dispatchers.Main) {
            writeDialog(message, rootView)
        }
    }

    // closeComunication()
    //      Esta función cierra la comunicación con el servidor. Notifica al servidor sobre la desconexión y cierra el socket.
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

    // writeDialog(message: String?, rootView: View)
    //      Esta función escribe un diálogo en la interfaz de usuario. Utiliza corrutinas para operaciones en el hilo principal.
    suspend fun writeDialog(message: String?, rootView: View) {
        withContext(Dispatchers.Main) {
            var chatSpaceView = rootView.findViewById<ChatSpaceView>(R.id.chatSpace)
            var messagFragments = message?.split("~")
            val userName = messagFragments?.get(0)
            val color = messagFragments?.get(1)?.toInt()
            val messageDecrypted = decryptMessage(messagFragments?.get(2)!!, "encriptadito")
            chatSpaceView.appendDialog(messagFragments?.get(2)!!, 1, color!!, userName)
        }
    }

    // setRootView(newRootView: View)
    //      Esta función establece la vista principal donde se mostrarán los mensajes.

    fun setRootView(newRootView: View) {
        parentView = newRootView
    }

    companion object {
        var clientColor = mutableListOf<Int>()
    }

    // Funciones criptográficas (decryptMessage, encryptMessage, generateKey):
    //      Funciones para desencriptar y encriptar mensajes utilizando cifrado AES
    //      Estas funciones se utilizan para cifrar y descifrar mensajes que se envían entre clientes.
    //      generateKey crea una clave secreta a partir de la clave proporcionada.

    fun decryptMessage(encryptedMessage: String, key: String): String {
        try {
            val cipher = Cipher.getInstance("AES")
            val secretKey = generateKey(key)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encryptedBytes = encryptedMessage.toByteArray(Charsets.UTF_8)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            println(e.stackTrace)
            return "Error al desencriptar el mensaje"
        }
    }

    fun encryptMessage(message: String, key: String): String {
        var messageEncripted: String = ""

    try {
        val cipher = Cipher.getInstance("AES")
        val secretKey = generateKey(key)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val messageEncriptedBytes: ByteArray = cipher.doFinal(message.toByteArray())

        messageEncripted = messageEncriptedBytes.toString(Charsets.UTF_8)

        return decryptMessage(messageEncripted, key)

        }catch (e : Exception)
        {
            messageEncripted = "Error al encriptar el mensaje"
            println(e.stackTrace)
        }
        return messageEncripted
    }

    @Throws(Exception::class)
    fun generateKey(key : String) :SecretKeySpec{
        val sha = MessageDigest.getInstance("SHA-256")
        var keyBytes = key.toByteArray(Charsets.UTF_8)

        keyBytes = sha.digest(keyBytes)

        val secretKey = SecretKeySpec(keyBytes, "AES")
        return secretKey
    }
}
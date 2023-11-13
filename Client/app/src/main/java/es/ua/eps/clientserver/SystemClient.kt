package es.ua.eps.clientserver

import android.view.View
import android.widget.TextView

// SystemClient es un objeto singleton que actúa como un punto de entrada y control para las operaciones relacionadas con el cliente.
// SystemClient actúa como una interfaz de alto nivel para realizar operaciones relacionadas con el cliente.
// Encapsula las operaciones del cliente y proporciona métodos convenientes para interactuar con ellas.
object SystemClient {

    //    Una propiedad privada que almacena la instancia del cliente asociado.
    private var client : Client? = null

    //     Establece la instancia del cliente con la proporcionada como argumento.
    fun setClient(newClient : Client)
    {
        client = newClient
    }

    //     Elimina la instancia del cliente, estableciéndola como nula.
    fun removeClient()
    {
        client = null
    }

    //     Devuelve la instancia actual del cliente.
    fun getClient() : Client?{
        return client
    }

    //     Inicia la conexión del cliente al servidor con un nombre de usuario proporcionado.
    suspend fun connectClientToServer(userName : String){
        client?.connectClientToServer(userName)
    }

    //     Cierra la comunicación con el servidor.
    suspend fun closeComunication()
    {
        client?.closeComunication()
    }

    //     Escribe la respuesta del servidor en la vista raíz proporcionada.
    suspend fun writeResponse(messageText: String?, viewRoot : View)
    {
        client?.writeResponse(messageText!!, viewRoot)
    }

    //     Envía un mensaje al servidor a través del cliente.
    suspend fun sendMessageToServer(messageText : String){
        client?.sendMessageToServer(messageText)
    }

    //     Establece la vista raíz para el cliente.
    fun setRootView(newRootView : View) {
        client?.setRootView(newRootView)
    }

    //     Verifica si el cliente está conectado al servidor.
    fun isclientConected() : Boolean {
        return client?.isConnectedToServer == true
    }

    //     Crea una sala de chat con el nombre proporcionado.
    suspend fun createChatRoom(roomName : String){
        client?.createChatRoom(roomName)
    }

    //     Se une a una sala de chat con el identificador proporcionado.
    suspend fun joinChatRoom(chatRoomId : Int) : Boolean{
        return client?.joinChatRoom(chatRoomId) == true
    }

    //     Abandona la sala de chat actual.
    suspend fun goOutChatRoom(){
        client?.goOutChatRoom()
    }

    //     Solicita la lista de salas de chat al servidor.
    suspend fun askForChatRoomList(){
        client?.askForChatRoomList()
    }

}
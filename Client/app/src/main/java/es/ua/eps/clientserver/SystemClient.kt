package es.ua.eps.clientserver

import android.view.View
import android.widget.TextView

object SystemClient {
    private var client : Client? = null

    fun setClient(newClient : Client)
    {
        client = newClient
    }

    fun removeClient()
    {
        client = null
    }

    fun getClient() : Client?{
        return client
    }

    suspend fun connectClientToServer(userName : String){
        client?.connectClientToServer(userName)
    }

    suspend fun closeComunication()
    {
        client?.closeComunication()
    }

    suspend fun writeResponse(messageText: String?, viewRoot : View)
    {
        client?.writeResponse(messageText!!, viewRoot)
    }

    suspend fun sendMessageToServer(messageText : String){
        client?.sendMessageToServer(messageText)
    }

    fun setRootView(newRootView : View) {
        client?.setRootView(newRootView)
    }

    fun isclientConected() : Boolean {
        return client?.isConnectedToServer == true
    }

    suspend fun createChatRoom(roomName : String){
        client?.createChatRoom(roomName)
    }

    suspend fun joinChatRoom(chatRoomId : Int) : Boolean{
        return client?.joinChatRoom(chatRoomId) == true
    }

    suspend fun goOutChatRoom(){
        client?.goOutChatRoom()
    }

    suspend fun askForChatRoomList(){
        client?.askForChatRoomList()
    }

    suspend fun waitResponseFromServer(){
        //client?.waitResponseFromServer()
    }
}
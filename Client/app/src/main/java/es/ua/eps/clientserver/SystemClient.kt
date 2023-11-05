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

    suspend fun connectClientToServer(){
        client?.connectClientToServer()
    }

    suspend fun closeComunication()
    {
        client?.closeComunication()
    }

    suspend fun writeResponse(viewRoot : View)
    {
        client?.writeResponse(viewRoot)
    }

    suspend fun sendMessageToServer(messageText : String){
        client?.sendMessageToServer(messageText)
    }

    fun setRootView(newRootView : View) {
        client?.setRootView(newRootView)
    }


}
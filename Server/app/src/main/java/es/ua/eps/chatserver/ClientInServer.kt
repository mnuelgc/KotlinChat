package es.ua.eps.chatserver

import android.graphics.Color
import java.io.InputStream
import java.net.Socket

class ClientInServer internal constructor(
    private val id : Int,
    private val socket : Socket) {
    private var name : String = ""
    private var color : Int = Int.MAX_VALUE

    public fun setName (name : String)
    {
        this.name = name
    }
    public fun setColor(newColor : Int)
    {
        this.color = newColor
    }

    public fun getId () : Int{
        return this.id
    }

    public fun getSocket() : Socket{
        return this.socket
    }

    public fun getName() : String{
        return this.name
    }

    public fun getColor() : Int{
        return this.color
    }

    public fun getInputStream() : InputStream{
        return socket.getInputStream()
    }
}
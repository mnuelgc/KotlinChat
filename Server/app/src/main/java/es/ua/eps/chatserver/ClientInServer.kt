package es.ua.eps.chatserver

import android.graphics.Color
import java.io.InputStream
import java.net.Socket

// Esta clase representa un cliente en el servidor. Tiene propiedades privadas para el ID, el socket de conexión, el nombre, el color y la sala de chat actual.
class ClientInServer internal constructor(
    private val id : Int,
    private val socket : Socket) {
    private var name : String = "NoName"
    private var color : Int = Int.MAX_VALUE
    public var actualRoom : ChatRoom? = null

    // setName(name : String)
    //       Esta función establece el nombre del cliente.
    //       Es un método público que toma un argumento name y asigna ese valor a la propiedad name del objeto ClientInServer.

    fun setName (name : String)
    {
        this.name = name
    }

    // setColor(newColor : Int)
    //       Aquí, la función setColor permite cambiar el color del cliente.
    //       Recibe un nuevo valor de color (newColor) y lo asigna a la propiedad color del objeto.

    fun setColor(newColor : Int)
    {
        this.color = newColor
    }

    // getSocket() : Socket
    //       La función getSocket devuelve el socket de conexión del cliente. Permite acceder al socket asociado al cliente.

    fun getSocket() : Socket{
        return this.socket
    }

    // getColor() : Int
    //        La función getColor devuelve el color del cliente. Proporciona acceso a la propiedad color del objeto.
    fun getColor() : Int{
        return this.color
    }

    // dataToTextFormat() : String
    //         La función dataToTextFormat crea una representación de texto de los datos del cliente.
    //         Concatena varias propiedades en una cadena de texto,
    //         incluyendo el ID del cliente, el nombre, el color y la ID de la sala de chat actual (si el cliente está en alguna sala).

    fun dataToTextFormat() : String {
        var dataText = ""

        dataText += "CLIENT_ID${this.id.toString()}"
        dataText += "CLIENT_NAME${this.name}"
        dataText += "CLIENT_COLOR_IN_GROUP${this.color.toString()}"
        dataText += "ACTUAL_CHATROOM${this.actualRoom?.getId().toString()}"

        return dataText
    }

}
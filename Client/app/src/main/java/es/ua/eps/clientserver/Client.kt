package es.ua.eps.clientserver

import android.widget.TextView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

class Client internal constructor(
    var dstAddress : String,
    var dsPort : Int,
    var textResponse : TextView
){
    var response = ""
    var socket: Socket? = null

    fun connectClientToServer(){
        socket = null
        try{
            socket = Socket(dstAddress, dsPort)

            val byteArrayOutputStream = ByteArrayOutputStream(1024)
            val buffer = ByteArray(1024)
            var bytesRead : Int
            val inputStream = socket!!.getInputStream()

            while (inputStream.read(buffer).also {bytesRead = it} != -1){
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                response += byteArrayOutputStream.toString("UTF-8")

            }

        }catch (ex : UnknownHostException)
        {
            ex.printStackTrace()
        }catch (ex : IOException)
        {
            ex.printStackTrace()
        }finally {
            try {
                socket?.close()
            }
            catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    fun writeResponse(){
        textResponse.text = response
    }


}
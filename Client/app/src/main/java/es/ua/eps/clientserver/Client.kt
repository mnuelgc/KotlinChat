package es.ua.eps.clientserver

import android.graphics.Color
import android.widget.TextView
import es.ua.eps.clientserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

class Client internal constructor(

    var textResponse : TextView,
    var viewBinding: ActivityMainBinding
){
    var dstAddress =""
    var dsPort = 0

    var isConnectedToServer = false
    var response = ""
    var socket: Socket? = null

    public fun setAddress(address :String){
        dstAddress = address
        dstAddress = address
    }

    public fun setPort(port :Int){
        dsPort = port
    }
/*
    suspend fun connectClientToServer(){

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
                    }


                   /* while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        response += byteArrayOutputStream.toString("UTF-8")
                        byteArrayOutputStream.flush()

                    }*/

                    while (true) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) {
                            // Se ha llegado al final del flujo de entrada
                            break
                        }

                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                    }

// Ahora tienes los datos en byteArrayOutputStream
                    val response = byteArrayOutputStream.toString("UTF-8")


                } catch (ex: UnknownHostException) {
                    ex.printStackTrace()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }
*/
    suspend fun connectClientToServer() {
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
                    }

                    while (true) { //Bucle de lectura de mensajes del server
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) {
                            // Se ha llegado al final del flujo de entrada
                            break
                        }

                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        response += byteArrayOutputStream.toString("UTF-8")

                        withContext(Dispatchers.Main){ writeResponse()}

                        }

                } catch (ex: UnknownHostException) {
                    ex.printStackTrace()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    suspend fun sendMessageToServer(message: String){
        if (isConnectedToServer) {
            val writer: PrintWriter = PrintWriter(socket!!.getOutputStream(), true)
            writer.println (message + "\n")
            writer.flush()
        }
    }

         /*   try {
/*
                socket!!.outputStream.write("Hello from the client!".toByteArray());
                var text : String = socket!!.inputStream.read().toString()
*/
               PrintStream(socket!!.getOutputStream(), true)
               // val reader = BufferedReader(InputStreamReader(socket!!.getInputStream(), "UTF-8"))
                val writer = PrintStream(socket!!.getOutputStream(), true)

                //ESCRITURA LA PRIMERA VEZ PARA IDENTIFICARSE
                writer.print(message)
                writer.flush()
          //      socket = Socket(dstAddress, dsPort)


            } catch (ex: UnknownHostException) {
                ex.printStackTrace()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }*/
   //     }
    //}

    fun writeResponse(){
        textResponse.text = response

        if (isConnectedToServer) {
            viewBinding.root.setBackgroundColor(Color.GREEN)
        }else{
            viewBinding.root.setBackgroundColor(Color.YELLOW)
        }

    }


    fun closeComunication()
    {
        if (isConnectedToServer)
        {
            response = "Disconnected From Server"
            isConnectedToServer = false
            socket?.shutdownOutput()
            socket?.close()
            socket = null
        }
    }


}
/*package com.example.androidserversocket

import android.R
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class MainActivity : Activity() {
    var info: TextView? = null
    var infoip: TextView? = null
    var msg: TextView? = null
    var message = ""
    var serverSocket: ServerSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        info = findViewById<View>(R.id.info) as TextView
        infoip = findViewById<View>(R.id.infoip) as TextView
        msg = findViewById<View>(R.id.msg) as TextView
        infoip!!.text = ipAddress
        val socketServerThread: Thread = Thread(SocketServerThread())
        socketServerThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }

    private inner class SocketServerThread : Thread() {
        var count = 0
        override fun run() {
            try {
                serverSocket = ServerSocket(Companion.SocketServerPORT)
                runOnUiThread {
                    info!!.text = ("I'm waiting here: "
                            + serverSocket!!.localPort)
                }
                while (true) {
                    val socket = serverSocket!!.accept()
                    count++
                    message += """#$count from ${socket.inetAddress}:${socket.port}
"""
                    runOnUiThread { msg!!.text = message }
                    val socketServerReplyThread: SocketServerReplyThread = SocketServerReplyThread(
                        socket, count
                    )
                    socketServerReplyThread.run()
                }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }

        companion object {
            const val SocketServerPORT = 8080
        }
    }

    private inner class SocketServerReplyThread internal constructor(
        private val hostThreadSocket: Socket,
        var cnt: Int
    ) :
        Thread() {
        override fun run() {
            val outputStream: OutputStream
            val msgReply = "Hello from Android, you are #$cnt"
            try {
                outputStream = hostThreadSocket.getOutputStream()
                val printStream = PrintStream(outputStream)
                printStream.print(msgReply)
                printStream.close()
                message += "replayed: $msgReply\n"
                runOnUiThread { msg!!.text = message }
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                message += "Something wrong! $e\n"
            }
            runOnUiThread { msg!!.text = message }
        }
    }

    private val ipAddress: String
        private get() {
            var ip = ""
            try {
                val enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces()
                while (enumNetworkInterfaces.hasMoreElements()) {
                    val networkInterface = enumNetworkInterfaces
                        .nextElement()
                    val enumInetAddress = networkInterface
                        .inetAddresses
                    while (enumInetAddress.hasMoreElements()) {
                        val inetAddress = enumInetAddress.nextElement()
                        if (inetAddress.isSiteLocalAddress) {
                            ip += """
                                SiteLocalAddress: ${inetAddress.hostAddress}
                                
                                """.trimIndent()
                        }
                    }
                }
            } catch (e: SocketException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                ip += "Something Wrong! $e\n"
            }
            return ip
        }
}*/
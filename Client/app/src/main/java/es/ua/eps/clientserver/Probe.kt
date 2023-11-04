/*package com.androidsrc.client

import android.os.AsyncTask
import android.widget.TextView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException

class Client internal constructor(
    var dstAddress: String,
    var dstPort: Int,
    var textResponse: TextView
) :
    AsyncTask<Void?, Void?, Void>() {
    var response = ""
    protected override fun doInBackground(vararg arg0: Void?): Void {
        var socket: Socket? = null
        try {
            socket = Socket(dstAddress, dstPort)
            val byteArrayOutputStream = ByteArrayOutputStream(
                1024
            )
            val buffer = ByteArray(1024)
            var bytesRead: Int
            val inputStream = socket.getInputStream()

            /*
             * notice: inputStream.read() will block if no data return
			 */while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                response += byteArrayOutputStream.toString("UTF-8")
            }
        } catch (e: UnknownHostException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            response = "UnknownHostException: $e"
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            response = "IOException: $e"
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        }
        return response
    }

    override fun onPostExecute(result: Void) {
        textResponse.text = response
        super.onPostExecute(result)
    }


}*/
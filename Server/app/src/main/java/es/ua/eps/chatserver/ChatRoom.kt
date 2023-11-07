package es.ua.eps.chatserver

import android.graphics.Color

const val MAX_CLIENTS = 5
class ChatRoom{
    private val id : Int
    private val clientsInRoom : ArrayList<ClientInServer>
    private var name : String
    private var isActive : Boolean
    private var colorsInGroup : ArrayList<Int>

    constructor(id: Int, name: String){
        this.id = id
        this.name = name
        this.clientsInRoom = ArrayList<ClientInServer>()
        isActive = true
        colorsInGroup = ArrayList<Int>()
    }
    constructor(id: Int, name: String, creator : ClientInServer)
    {
        this.id = id
        this.clientsInRoom = ArrayList<ClientInServer>()
        this.clientsInRoom.add(creator)
        this.name = name
        isActive = true
        colorsInGroup = ArrayList<Int>()
        colorsInGroup.add(Color.parseColor("#FF0000"))
        colorsInGroup.add(Color.parseColor("#FFFF00"))
        colorsInGroup.add(Color.parseColor("#FF00FF"))
        colorsInGroup.add(Color.parseColor("#00FFFF"))
        colorsInGroup.add(Color.parseColor("#F000F0"))

        creator.setColor(colorsInGroup.last())
        creator.actualRoom = this
    }
    fun getId() : Int{
        return id
    }

    fun getName() : String{
        return this.name
    }

    fun howManyClients() : Int {
        return clientsInRoom.count()
    }

    fun clientGetIn(client : ClientInServer) : Boolean{

        if (this.id == 0){
            clientsInRoom.add(client)
            client.actualRoom = this
            return true
        }
        if (clientsInRoom.count() < MAX_CLIENTS)
        {
            client.actualRoom = this
            val color = colorsInGroup.last()
            client.setColor(color)
            clientsInRoom.add(client)
            colorsInGroup.remove(color)

            if (!isActive) isActive = true
            return true
        }else{
            return false
        }
    }

    fun clientGoOut(client : ClientInServer){
        clientsInRoom.remove(client)

        if (id != 0) {
            colorsInGroup.add(client.getColor())

            if (clientsInRoom.isEmpty()) {
                isActive = false
            }
        }
    }


    fun isRoomActive() : Boolean{
        return isActive
    }

    fun getClients () :  ArrayList<ClientInServer>{
        return clientsInRoom
    }

    fun wipeRoom()
    {
        clientsInRoom.clear()
    }
}
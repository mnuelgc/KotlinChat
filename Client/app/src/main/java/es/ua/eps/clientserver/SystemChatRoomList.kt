package es.ua.eps.clientserver

object SystemChatRoomList {
    var mutableMap : MutableMap<Int, String> =mutableMapOf<Int, String>()

    fun addRoom(key : Int, name: String)
    {
        mutableMap[key - 1] = name
    }

}
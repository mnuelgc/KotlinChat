package es.ua.eps.clientserver

// Este es un objeto singleton, lo que significa que solo hay una instancia de esta clase en el programa.
// SystemChatRoomList proporciona una única instancia que se puede acceder globalmente
// Y ofrece una forma de gestionar la lista de salas de chat en el sistema.
// La función addRoom se encarga de agregar nuevas salas a la lista.

object SystemChatRoomList {
    // Esta es una propiedad mutable que almacena la lista de salas de chat.
    // Es un MutableMap donde la clave (Int) es el identificador de la sala y el valor (String) es el nombre de la sala.

    var mutableMap : MutableMap<Int, String> =mutableMapOf<Int, String>()

    //    Esta función se utiliza para agregar una sala de chat a la lista.
    //    Toma dos parámetros: key, que es el identificador de la sala, y name, que es el nombre de la sala.
    //    Resta 1 al key antes de agregar la sala al mutableMap, ya que en el servidor hay una sala extra que es el lobby
    fun addRoom(key : Int, name: String)
    {
        mutableMap[key - 1] = name
    }

}
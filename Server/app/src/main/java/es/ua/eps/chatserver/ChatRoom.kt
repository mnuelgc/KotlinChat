package es.ua.eps.chatserver

//Aquí se define una constante MAX_CLIENTS con un valor de 5, que representa el número máximo de clientes permitidos en una sala de chat.

const val MAX_CLIENTS = 5

//  Se define la clase ChatRoom con varias propiedades privadas: id para la identificación de la sala,
//  clientsInRoom para almacenar los clientes en la sala, name para el nombre de la sala, isActive para indicar si la sala está activa,
//  y idColorsInGroup para almacenar los colores disponibles en la sala.
class ChatRoom {
    private val id: Int
    private val clientsInRoom: ArrayList<ClientInServer>
    private var name: String
    private var isActive: Boolean
    private var idColorsInGroup: ArrayList<Int>

    //  Se proporcionan dos constructores. El primero se utiliza para crear una sala sin un creador específico (Usada para cuando se crea el Lobby),
    //  y el segundo se utiliza cuando se crea una sala con un cliente que la creó.

    //  Este es el constructor principal que se utiliza cuando se crea el lobby.
    //  Inicializa las propiedades de la sala, como el ID, el nombre, la lista de clientes, el estado activo (inicializado como true)
    //  y la lista de colores disponibles.
    constructor(id: Int, name: String) {
        this.id = id
        this.name = name
        this.clientsInRoom = ArrayList<ClientInServer>()
        isActive = true
        idColorsInGroup = ArrayList<Int>()
    }


    //  Este constructor se utiliza cuando se crea una sala con un cliente específico como creador.
    //  Agrega al creador a la lista de clientes, inicializa otras propiedades y asigna un color al cliente creador.
    constructor(id: Int, name: String, creator: ClientInServer) {
        this.id = id
        this.clientsInRoom = ArrayList<ClientInServer>()
        this.clientsInRoom.add(creator)
        this.name = name
        isActive = true
        idColorsInGroup = ArrayList<Int>()
        for (i in 0 ..< Server.serverColors.count()) { idColorsInGroup.add(i) }

        var color = idColorsInGroup.first()
        creator.setColor(color)
        idColorsInGroup.remove(color)
        creator.actualRoom = this
    }

    // getId() : Int
    //      Devuelve el ID de la sala.
    fun getId(): Int {
        return id
    }

    // getName() : String
    //      Devuelve el nombre de la sala.
    fun getName(): String {
        return this.name
    }

    // howManyClients() : Int
    //      devuelve la cantidad de clientes en la sala.
    fun howManyClients(): Int {
        return clientsInRoom.count()
    }

    // clientGetIn(client: ClientInServer): Boolean
    //      Maneja la entrada de un cliente a la sala.
    //      Si la sala es el lobby (ID 0) o hay espacio disponible para un nuevo cliente,
    //      agrega al cliente a la sala
    //      tambien asigna un color al cliente si entra en una de las salas de chat
    //      devuelve true si ha podido entrar. En caso contrario, devuelve false.

    fun clientGetIn(client: ClientInServer): Boolean {

        if (this.id == 0) {
            clientsInRoom.add(client)
            client.actualRoom = this
            return true
        }
        if (clientsInRoom.count() < MAX_CLIENTS) {
            client.actualRoom = this
            val color = idColorsInGroup.first()
            client.setColor(color)
            clientsInRoom.add(client)
            idColorsInGroup.remove(color)

            if (!isActive) isActive = true
            return true
        } else {
            return false
        }
    }

    // clientGoOut(client: ClientInServer)
    //      Maneja la salida de un cliente de la sala.
    //      Elimina al cliente de la lista de clientes y
    //      si la sala no es el lobby devuelve su color al grupo de colores disponibles.
    fun clientGoOut(client: ClientInServer) {
        clientsInRoom.remove(client)

        if (id != 0) {
            idColorsInGroup.add(client.getColor())

            if (clientsInRoom.isEmpty()) {
                isActive = false
            }
        }
    }

    // isRoomActive(): Boolean
    //      Devuelve si la sala está activa o no.
    fun isRoomActive(): Boolean {
        return isActive
    }

    // fun getClients(): ArrayList<ClientInServer>
    //      Devuelve la lista de clientes en la sala.
    fun getClients(): ArrayList<ClientInServer> {
        return clientsInRoom
    }

    // wipeRoom()
    //      Limpia la sala, eliminando todos los clientes de la lista.
    fun wipeRoom() {
        clientsInRoom.clear()
    }

    // dataToTextFormat(): String
    //      Devuelve una representación de texto de los datos de la sala, incluyendo ID, clientes, estado activo y colores disponibles.
    public fun dataToTextFormat(): String {
        var dataToText = ""

        dataToText += "CHAT_ID${this.id.toString()}"
        dataToText += "CLIENTS{"
        for (i in 0 until this.clientsInRoom.count()) {
            dataToText += "CLIENT$i/${clientsInRoom[i].dataToTextFormat()}"
            if(i != this.clientsInRoom.count() -1)
                dataToText += ";;"
        }
        dataToText+= "}"
        dataToText += "IS_ACTIVE${this.isActive.toString()}"
        dataToText += "COLORS{"

        for (i in 0 until this.idColorsInGroup.count()) {
            dataToText += "COLOR$i/${idColorsInGroup[i].toString()}"
            if(i != this.idColorsInGroup.count() -1)
                dataToText += ";;"
        }
        dataToText+= "}"
        dataToText+= ";;"

        return dataToText
    }

    // minimaldataToTextFormat(): String
    //      Devuelve una representación de texto más pequeña de los datos de la sala, incluyendo solo ID y nombre
    //      datos minimos y fundamentales para gestionar las salas en el cliente.
    public fun minimaldataToTextFormat(): String {
        var minimalDataToText = ""
        minimalDataToText += "ID_SALA$id~"
        minimalDataToText += "NAME_SALA$name"
        return minimalDataToText
    }



}
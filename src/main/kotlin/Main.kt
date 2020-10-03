import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pbx.Model
import pbx.NodeGrpcKt

suspend fun main(args: Array<String>) {
    val tinodeClient = TinodeClient()

    runBlocking { tinodeClient.init() }
    runBlocking { tinodeClient.login("username", "password") }
    runBlocking { tinodeClient.createTopic("hello") }
    Thread.sleep(5000)
}

class TinodeClient {
    val channel = Channel<Model.ClientMsg>()
    lateinit var grpcChannel: ManagedChannel
    lateinit var nodeStub: NodeGrpcKt.NodeCoroutineStub

    suspend fun init() {
        this.connect()
        val stream = nodeStub.messageLoop(channel.consumeAsFlow())
        GlobalScope.launch {
            stream.collect { value ->
                println(value)
            }
        }

        hi()
    }

    private fun connect() {
        this.grpcChannel = ManagedChannelBuilder
            .forAddress("localhost", 16060)
            .usePlaintext()
            .build()

        this.nodeStub = NodeGrpcKt.NodeCoroutineStub(this.grpcChannel)
    }
    fun disconnect() {
        this.grpcChannel.shutdownNow()
    }

    suspend fun hi() {
        val hiMessage =
            this.generateDefaultMessage()
            .setHi(hiMessage())
            .build()

        channel.send(hiMessage)
    }

    private fun generateDefaultMessage(): Model.ClientMsg.Builder {
        return Model.ClientMsg.newBuilder()
    }

    suspend fun login(username: String, password: String) {
        val loginMessage = this.generateDefaultMessage()
            .setLogin(loginMessage(username, password))
            .build()

        this.channel.send(loginMessage)
    }

    suspend fun createTopic(tag: String) {
        val topicMessage =
            this.generateDefaultMessage()
                .setSub(this.createTopicMessage(tag))
                .build()

        this.channel.send(topicMessage)
    }

    private fun hiMessage(): Model.ClientHi {
        return Model.ClientMsg
            .newBuilder()
            .hi
            .toBuilder()
            .setVer("0.16.7")
            .build()
    }

    private fun createTopicMessage(tag: String): Model.ClientSub? {
        return this.generateDefaultMessage()
            .subBuilder
            .setTopic("new")
            .setSetQuery(
                Model
                    .SetQuery
                    .newBuilder()
                    .addTags(tag)
                    .build()
            )
            .build()
    }

    private fun createAccountMessage(username: String, password: String): Model.ClientAcc {
        return this.generateDefaultMessage()
            .accBuilder
            .setLogin(true)
            .setScheme("basic")
            .setUserId("new$username") // must starts with "new" keyword
            .setSecret(ByteString.copyFrom("username:$password", "UTF-8"))
            .build()
    }

    private fun loginMessage(username: String, password: String): Model.ClientLogin? {
        return Model
            .ClientMsg
            .newBuilder()
            .loginBuilder
            .setScheme("basic")
            .setSecret(ByteString.copyFromUtf8("$username:$password"))
            .build()
    }
}

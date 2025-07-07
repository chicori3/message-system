package io.chicori3.messagesystem.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.chicori3.messagesystem.dto.Message
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ArrayBlockingQueue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageHandlerTest(
    @LocalServerPort
    private var port: Int,
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : StringSpec({

    "Direct Chat Basic Test" {
        // given
        val url = "ws://localhost:$port/ws/v1/message"
        val leftQueue = ArrayBlockingQueue<String>(1)
        val leftClient = StandardWebSocketClient()
        val leftWebSocketSession = leftClient.execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    leftQueue.put(message.payload)
                }
            },
            url,
        ).get()

        val rightQueue = ArrayBlockingQueue<String>(1)
        val rightClient = StandardWebSocketClient()
        val rightWebSocketSession = rightClient.execute(
            object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    rightQueue.put(message.payload)
                }
            },
            url,
        ).get()

        val leftMessage = TextMessage(objectMapper.writeValueAsString(Message("hello")))
        val rightMessage = TextMessage(objectMapper.writeValueAsString(Message("world")))

        // when
        leftWebSocketSession.sendMessage(leftMessage)
        rightWebSocketSession.sendMessage(rightMessage)

        // then
        leftQueue.take() shouldContain "world"
        rightQueue.take() shouldContain "hello"

        leftWebSocketSession.close()
        rightWebSocketSession.close()
    }
}) {
    override fun extensions() = listOf(SpringExtension)
}

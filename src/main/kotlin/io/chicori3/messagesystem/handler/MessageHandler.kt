package io.chicori3.messagesystem.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.chicori3.messagesystem.dto.Message
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class MessageHandler(
    private val mapper: ObjectMapper,
) : TextWebSocketHandler() {

    private var leftSide: WebSocketSession? = null
    private var rightSide: WebSocketSession? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info { "connection established: ${session.id}" }

        when {
            leftSide == null -> leftSide = session
            rightSide == null -> rightSide = session
            else -> {
                log.warn { "too many connection: ${session.id}" }
                session.close()
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error { "transport error: [${exception.message}] from ${session.id}" }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info { "connection closed: [$status] from ${session.id}" }

        when (session) {
            leftSide -> leftSide = null
            rightSide -> rightSide = null
            else -> log.warn { "unknown session: ${session.id}" }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info { "message received: [$message] from ${session.id}" }

        val payload = message.payload

        try {
            val incomingMessage = mapper.readValue<Message>(payload)

            when (session) {
                leftSide -> rightSide?.let { sendMessage(it, incomingMessage.content) }
                rightSide -> leftSide?.let { sendMessage(it, incomingMessage.content) }
                else -> log.warn { "unknown session: ${session.id}" }
            }
        } catch (e: Exception) {
            log.error { "failed to parse message: [${e.message}] from ${session.id}" }
            sendMessage(session, "failed to parse message")
        }
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        try {
            val formattedMessage = mapper.writeValueAsString(Message(message))
            session.sendMessage(TextMessage(formattedMessage))

            log.info { "message sent: [$formattedMessage] to ${session.id}" }
        } catch (e: Exception) {
            log.error { "failed to send message: [${e.message}] to ${session.id}" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {  }
    }
}

package io.chicori3.messagesystem.config

import io.chicori3.messagesystem.handler.MessageHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketHandlerConfig(
    private val messageHandler: MessageHandler,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(messageHandler, "/ws/v1/message")
            .setAllowedOrigins("*")
    }
}

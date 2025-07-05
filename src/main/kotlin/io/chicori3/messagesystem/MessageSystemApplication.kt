package io.chicori3.messagesystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MessageSystemApplication

fun main(args: Array<String>) {
    runApplication<MessageSystemApplication>(*args)
}

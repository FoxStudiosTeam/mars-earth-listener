package ru.foxstudios.marsearthlistener

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.socket.DatagramPacket
import reactor.core.publisher.Flux
import reactor.netty.udp.UdpServer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val server = UdpServer.create().port(25578).host("0.0.0.0").wiretap(true).handle { inbound, outbound ->
        val inFlux: Flux<DatagramPacket> = inbound.receiveObject()
            .handle { incoming, sink ->
                if (incoming is DatagramPacket) {
                    val packet = incoming
                    val content = packet.content().toString(StandardCharsets.UTF_8)
                    println(content.toByteArray().size)
                    try {
                        val url = URI("http://localhost:30007/data/addschedule").toURL()
                        val connection = url.openConnection() as HttpURLConnection

                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.doOutput = true

                        val requestBody = content

                        val outputStream: OutputStream = connection.outputStream
                        outputStream.write(requestBody.toByteArray())
                        outputStream.flush()

                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        var line: String?
                        val responsd = StringBuffer()
                        while (reader.readLine().also { line = it } != null) {
                            responsd.append(line)
                        }
                        reader.close()

                        // Закрытие соединения
                        connection.disconnect()

                        // Вывод ответа
                        println(responsd.toString())
                    } catch (e: Exception) {
                        println(e.message)
                    }

                    val byteBuf: ByteBuf = Unpooled.copiedBuffer("ok", StandardCharsets.UTF_8)
                    val response = DatagramPacket(byteBuf, packet.sender())
                    sink.next(response)
                }
            }
        return@handle outbound.sendObject(inFlux)
    }
    server.bindNow().onDispose().block()
}

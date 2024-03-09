package ru.foxstudios.marsearthlistener

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.socket.DatagramPacket
import reactor.core.publisher.Flux
import reactor.netty.udp.UdpServer
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    val server = UdpServer.create().port(25578).host("127.0.0.1").wiretap(true).handle { inbound, outbound ->
        val inFlux: Flux<DatagramPacket> = inbound.receiveObject()
            .handle { incoming, sink ->
                if (incoming is DatagramPacket) {
                    val packet = incoming
                    val content = packet.content().toString(StandardCharsets.UTF_8)
                    println(content.toByteArray().size)
                    val url = URI("http://127.0.0.1:30007/data/addschedule").toURL()
                    val con = url.openConnection() as HttpURLConnection
                    con.requestMethod = "POST"
                    con.doOutput = true
                    con.setRequestProperty("Content-Type", "application/json")
                    con.setRequestProperty("Accept", "application/json")
                    con.outputStream.use{
                        output -> output.write(content.toByteArray(StandardCharsets.UTF_8))
                    }
                    println(content)
                    con.disconnect()

                    val byteBuf: ByteBuf = Unpooled.copiedBuffer("ok", StandardCharsets.UTF_8)
                    val response = DatagramPacket(byteBuf, packet.sender())
                    sink.next(response)
                }
            }
        return@handle outbound.sendObject(inFlux)
    }
    server.bindNow().onDispose().block()
}

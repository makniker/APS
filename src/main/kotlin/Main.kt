package org.example

import java.nio.file.Files
import java.nio.file.Paths


fun main() {
    val config = "config.txt"
    try {
        val url = ClassLoader.getSystemResource(config)
        val lines = Files.readAllLines(Paths.get(url.toURI()))
        val mode: AppMode = if (lines[3] == "AUTO") {
            AppMode.AUTO
        } else if (lines[3] == "STEP") {
            AppMode.STEP
        } else {
            throw Exception("Wrong config!")
        }
        val app = Application(lines[0].toInt(), lines[1].toInt(), lines[2].toInt(), mode, lines[4].toLong(), lines[5].toLong(), lines[6].toDouble())
        app.start()
    } catch (e: Exception) {
        println("Wrong config - ${e.message}")
    }
}
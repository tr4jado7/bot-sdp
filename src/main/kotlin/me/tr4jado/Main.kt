package me.tr4jado

import me.tr4jado.bot.Bot
import me.tr4jado.bot.services.ConfigReader.loadConfig

fun main() {
    val config = loadConfig("config/geral.yml")
    val bot = Bot(config["discord_token"] as String)
    bot.start()
}
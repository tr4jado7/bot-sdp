package me.tr4jado.bot

import me.tr4jado.bot.listeners.CommandListener
import me.tr4jado.bot.listeners.EventListener
import me.tr4jado.bot.services.CommandRegistry
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

lateinit var bot: JDA

class Bot(private val token: String) {
    fun start() {
        bot = JDABuilder.createDefault(token)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT
            )
            .addEventListeners(EventListener(), CommandListener())
            .build()

        CommandRegistry.registerCommand(bot)
        bot.awaitReady()
    }
}
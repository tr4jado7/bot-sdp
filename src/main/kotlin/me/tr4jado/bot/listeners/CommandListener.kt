package me.tr4jado.bot.listeners

import me.tr4jado.bot.commands.EmbedCommand
import me.tr4jado.bot.commands.MessageCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandListener : ListenerAdapter() {
    val embedCommand = EmbedCommand()
    val messageCommand = MessageCommand()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            embedCommand.command -> embedCommand.handlerSlashCommand(event)
            messageCommand.command -> messageCommand.handlerSlashCommand(event)
        }
    }
}
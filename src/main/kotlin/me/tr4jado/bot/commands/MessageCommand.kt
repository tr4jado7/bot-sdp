package me.tr4jado.bot.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MessageCommand {
    val command = "message"

    val ticketCommand = TicketCommand()

    fun handlerSlashCommand(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return
        if (event.name != command) return

        when (event.getOption("type")?.asString) {
            ticketCommand.suffix -> ticketCommand.handlerCommand(event)
        }
    }
}
package me.tr4jado.bot.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

class SayCommand {
    val command = "say"

    fun handlerSlashCommand(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return
        if (event.name != command) return

        val messageInput = TextInput.create("message", "Mensagem a ser enviada", TextInputStyle.PARAGRAPH)
            .setRequired(true)
            .build()

        val modal = Modal.create("say", "Enviar Mensagem")
            .addActionRow(messageInput)
            .build()

        event.replyModal(modal).queue()
    }
}
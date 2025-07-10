package me.tr4jado.bot.services

import me.tr4jado.bot.commands.EmbedCommand
import me.tr4jado.bot.commands.MessageCommand
import me.tr4jado.bot.commands.SayCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction

object CommandRegistry {
    fun registerCommand(jda: JDA) {
        val commands: CommandListUpdateAction = jda.updateCommands()

        commands.addCommands(
            Commands.slash(EmbedCommand().command, "Criar um embed no chat atual.")
                .addOption(OptionType.STRING, "json", "Json com informações do embed", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        )

        commands.addCommands(
            Commands.slash(MessageCommand().command, "Envia a mensagem de uma função.")
                .addOption(OptionType.STRING, "type", "Tipo da mensagem")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        )

        commands.addCommands(
            Commands.slash(SayCommand().command, "Enviar uma mensagem no chat atual.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        )

        commands.queue()
    }
}
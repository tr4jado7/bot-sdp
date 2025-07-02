package me.tr4jado.bot.commands

import me.tr4jado.bot.services.ConfigReader.loadConfig
import me.tr4jado.bot.utils.EmbedCreate.createConfigEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class TicketCommand {
    val suffix = "ticket"
    val config = loadConfig("config/ticket.yml")

    var count = 1

    fun handlerCommand(event: SlashCommandInteractionEvent) {
        @Suppress("UNCHECKED_CAST")
        val embedConfig = config["embed_public"] as? Map<String, Any> ?: return event.reply("Configuração de embed não encontrada.").setEphemeral(true).queue()
        @Suppress("UNCHECKED_CAST")
        val menuConfig = config["menu_options"] as? List<Map<String, Any>> ?: return event.reply("Configuração de opções do menu não encontrada.").setEphemeral(true).queue()

        try {
            val menu = StringSelectMenu.create("ticket:${count}")
                .setPlaceholder("Selecione uma opção")
                .apply {
                    menuConfig.forEach { option ->
                        addOption(
                            option["label"].toString(),
                            option["value"].toString(),
                            option["description"]?.toString() ?: "",
                        )
                    }
                }
                .build()

            count++

            val embed = createConfigEmbed(embedConfig).build()

            event.channel.sendMessageEmbeds(embed).setActionRow(menu).queue()
            event.reply("Mensagem enviada com sucesso.").setEphemeral(true).queue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
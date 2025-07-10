package me.tr4jado.bot.events

import me.tr4jado.bot.bot
import me.tr4jado.bot.services.ConfigReader.loadConfig
import me.tr4jado.bot.utils.EmbedCreate.createConfigEmbed
import me.tr4jado.bot.utils.TranscriptGenerator
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.FileUpload
import java.util.concurrent.TimeUnit

class BotEvents {
    val ticketConfig = loadConfig("config/ticket.yml")

    fun handleBotReady(event: ReadyEvent) {
        event.jda.guilds.forEach { guild ->
            guild.loadMembers().onSuccess { members ->
                members.forEach { member ->
                    if (member.roles.isEmpty()) {
                        MemberEvents().addDefaultRoles(member)
                    }
                }
            }
        }
    }

    fun handleStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.guild == null) return
        if (event.member == null) return

        val parts = event.componentId.split(":")

        when (parts[0]) {
            "ticket" -> {
                val selectedOption = event.values[0]

                event.reply("Você está preste criar um ticket ``$selectedOption``\nDeseja continuar?")
                    .addActionRow(
                        Button.success("ticket:create:$selectedOption", "Confirmar")
                    )
                    .setEphemeral(true)
                    .queue()
            }
        }
    }

    fun handleButtonInteraction(event: ButtonInteractionEvent) {
        if (event.guild == null) return
        if (event.member == null) return

        val channel = event.channel
        val member = event.member ?: return event.reply("Membro não encontrado.").setEphemeral(true).queue()
        val guild = event.guild ?: return event.reply("Servidor não encontrado.").setEphemeral(true).queue()
        val parts = event.componentId.split(":")

        when (parts[0]) {
            "ticket" -> {
                if (parts[1] == "create") {
                    val channelName = "${parts[2]}-${event.user.name}"
                    val existingChannel = event.guild?.getTextChannelsByName(channelName, true)?.firstOrNull()

                    if (existingChannel != null) {
                        event.reply("Você já possui um ticket aberto: ${existingChannel.asMention}").setEphemeral(true).queue()
                        return
                    }

                    if (parts[2] == "report") {
                        val idInput = TextInput.create("id", "ID do usuário", TextInputStyle.SHORT)
                            .setPlaceholder("Digite o ID do usuário")
                            .setRequired(true)
                            .build()

                        val reasonInput = TextInput.create("reason", "Motivo do relatório", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Digite o motivo do relatório")
                            .setRequired(true)
                            .build()

                        val evidenceInput = TextInput.create("evidence", "Evidências", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Adicione links das evidências")
                            .setRequired(true)
                            .build()

                        val modal = Modal.create(event.componentId, "Relatório")
                            .addActionRow(idInput)
                            .addActionRow(reasonInput)
                            .addActionRow(evidenceInput)
                            .build()

                        event.replyModal(modal).queue()
                    } else {
                        val newChannel = createNewTicketChannel(channelName, guild, member, event)

                        @Suppress("UNCHECKED_CAST")
                        val embedBuilder = createConfigEmbed(ticketConfig["embed_private"] as Map<String, Any>)
                        val embed = embedBuilder
                            .setDescription(
                                embedBuilder.descriptionBuilder.toString()
                                    .replace("{user}", member.asMention)
                                    .replace("{selected}", parts[2])
                            )
                            .setTimestamp(java.time.Instant.now())
                            .build()

                        event.reply("Seu ticket foi criado com sucesso.\nAcesse: ${newChannel.asMention}").setEphemeral(true).queue()

                        newChannel.sendMessageEmbeds(embed).setActionRow(
                            Button.danger("ticket:close", "Fechar Ticket")
                        ).queue()
                    }
                } else if (parts[1] == "close") {
                    event.reply("O ticket será fechado em 3 segundos.").setEphemeral(true).queue {
                        channel.delete().queueAfter(3, TimeUnit.SECONDS)
                    }

                    val user = findUserFromChannelName(bot, channel.name)

                    val messages = channel.iterableHistory.complete()
                    val transcript = TranscriptGenerator().generateTranscriptFile(channel.asTextChannel(), messages)
                    val fileUpload = FileUpload.fromData(transcript, "transcript_${channel.name}.html")

                    val embed = EmbedBuilder()
                        .setTitle(":lock: Ticket Fechado por ${member.user.name}")
                        .setDescription(":white_check_mark: As mensagens do ticket foram transcritas e estão anexadas a esta notificação. Baixe e abra o arquivo HTML para visualizar!")
                        .addField(":lock: Fechado por:", member.asMention, true)
                        .addField(":round_pushpin: Criado por:", user?.asMention ?: "no user", true)
                        .addField(
                            ":page_facing_up: Transcrição realizada em:",
                            "<t:${System.currentTimeMillis() / 1000}>",
                            true
                        )
                        .addField(":ticket: Nome do Ticket:", channel.name, true)
                        .setFooter("© 2025 Sonho de Pivete - All Rights Reserved.", "https://i.imgur.com/55qczSU.png")
                        .build()

                    user?.openPrivateChannel()?.queue { privateChannel ->
                        privateChannel.sendMessageEmbeds(embed).addFiles(fileUpload).queue()
                    }

                    transcript.delete()
                }
            }
        }
    }

    fun handleModalInteraction(event: ModalInteractionEvent) {
        val member = event.member
        val guild = event.guild

        if (guild == null || member == null) return

        val parts = event.modalId.split(":")

        if (parts[0] == "ticket" && parts[2] == "report") {
            val id = event.getValue("id")?.asString ?: return event.reply("ID do usuário não fornecido.").setEphemeral(true).queue()
            val reason = event.getValue("reason")?.asString ?: return event.reply("Motivo do relatório não fornecido.").setEphemeral(true).queue()
            val evidence = event.getValue("evidence")?.asString ?: ""

            val channelName = "${parts[2]}-${event.user.name}"
            val existingChannel = guild.getTextChannelsByName(channelName, true).firstOrNull()

            if (existingChannel != null) {
                event.reply("Você já possui um ticket aberto: ${existingChannel.asMention}").setEphemeral(true).queue()
                return
            }

            val newChannel = createNewTicketChannel(channelName, guild, member, event)

            event.reply("Seu ticket foi criado com sucesso.\nAcesse: ${newChannel.asMention}").setEphemeral(true).queue()

            @Suppress("UNCHECKED_CAST")
            val embedBuilder = createConfigEmbed(ticketConfig["embed_report"] as Map<String, Any>)
            val embed = embedBuilder
                .setDescription(
                    embedBuilder.descriptionBuilder.toString()
                        .replace("{user}", member.asMention)
                        .replace("{id}", id)
                )
                .apply {
                    val newFields = mutableListOf<MessageEmbed.Field>()

                    embedBuilder.fields.forEach { field ->
                        val newValue = field.value.toString()
                            .replace("{reason}", reason)
                            .replace("{evidences}", evidence)
                            .replace("{id}", id)

                        newFields.add(MessageEmbed.Field(field.name, newValue, field.isInline))
                    }

                    clearFields()
                    newFields.forEach { addField(it) }
                }
                .setTimestamp(java.time.Instant.now())
                .build()

            newChannel.sendMessageEmbeds(embed).setActionRow(
                Button.danger("ticket:close", "Fechar Ticket")
            ).queue()
        } else if (parts[0] == "say") {
            val message = event.getValue("message")?.asString ?: return event.reply("Mensagem não fornecida.").setEphemeral(true).queue()
            val channel = event.channel.asTextChannel()

            channel.sendMessage(message).queue()
            event.reply("Mensagem enviada com sucesso!").setEphemeral(true).queue()
        } else {
            event.reply("Modal não reconhecido.").setEphemeral(true).queue()
        }
    }

    private fun findUserFromChannelName(jda: JDA, channelName: String): User? {
        val parts = channelName.split("-")
        if (parts.size < 2) return null

        val username = parts[1]

        return try {
            val userId = username.toLong()
            jda.retrieveUserById(userId).complete()
        } catch (_: NumberFormatException) {
            jda.getUsersByName(username, true).firstOrNull()
                ?: jda.getUserByTag(username)
        }
    }

    private fun createNewTicketChannel(channelName: String, guild: Guild, member: Member, event: GenericInteractionCreateEvent): TextChannel {
        if (event !is ButtonInteractionEvent && event !is ModalInteractionEvent) {
            throw IllegalArgumentException("Tipo de evento não suportado")
        }

        val category = guild.getCategoryById(ticketConfig["ticket_category"] as String)
        val newChannel = guild.createTextChannel(channelName).setParent(category).complete()

        if (newChannel is TextChannel) {
            newChannel.upsertPermissionOverride(guild.publicRole).deny(Permission.VIEW_CHANNEL).queue()
            newChannel.upsertPermissionOverride(member).grant(Permission.VIEW_CHANNEL).queue()

            return newChannel
        }

        throw IllegalStateException("Não foi possível criar o canal de ticket.")
    }
}
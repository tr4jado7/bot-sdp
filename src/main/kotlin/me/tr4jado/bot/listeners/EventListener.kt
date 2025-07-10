package me.tr4jado.bot.listeners

import me.tr4jado.bot.events.*
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener : ListenerAdapter() {
    private val guildEvents = MemberEvents()
    private val botEvents = BotEvents()

    override fun onReady(event: ReadyEvent) {
        botEvents.handleBotReady(event)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        botEvents.handleButtonInteraction(event)
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        botEvents.handleStringSelectInteraction(event)
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        botEvents.handleModalInteraction(event)
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        guildEvents.handleGuildMemberJoin(event)
    }
}
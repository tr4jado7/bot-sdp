package me.tr4jado.bot.events

import me.tr4jado.bot.services.ConfigReader.loadConfig
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

class GuildEvents {
    fun handleGuildMemberJoin(event: GuildMemberJoinEvent) {
        addDefaultRoles(event.member)
    }

    fun addDefaultRoles(member: Member) {
        val guild = member.guild
        val config = loadConfig("config/geral.yml")

        try {
            val defaultRoles = config["default_roles"] as? List<*>

            defaultRoles?.forEach { roleObj ->
                val roleId = when (roleObj) {
                    is String -> roleObj
                    is Number -> roleObj.toString()
                    else -> {
                        return@forEach
                    }
                }

                try {
                    val role = guild.getRoleById(roleId)

                    if (role != null) {
                        guild.modifyMemberRoles(member, listOf(role), null).queue()
                    }
                } catch (e: NumberFormatException) {
                    println("ID de cargo inválido: $roleId")
                }
            } ?: println("Configuração de default_roles não encontrada ou não é uma lista")
        } catch (e: Exception) {
            println("Erro ao processar cargos padrão: ${e.message}")
        }
    }
}
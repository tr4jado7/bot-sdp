package me.tr4jado.bot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import org.json.JSONException
import org.json.JSONObject

class EmbedCommand {
    val command = "embedcreate"

    fun handlerSlashCommand(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return
        if (event.name != command) return

        try {
            val jsonOption: OptionMapping = event.getOption("json") ?: run {
                event.reply("Por favor, forneça um JSON válido.").setEphemeral(true).queue()
                return
            }

            val jsonString = jsonOption.asString
            val jsonObject = JSONObject(jsonString)
            val embedBuilder = EmbedBuilder()

            if (jsonObject.has("title")) {
                embedBuilder.setTitle(jsonObject.getString("title"))
            }

            if (jsonObject.has("description")) {
                embedBuilder.setDescription(jsonObject.getString("description"))
            }

            if (jsonObject.has("url")) {
                embedBuilder.setUrl(jsonObject.getString("url"))
            }

            if (jsonObject.has("color")) {
                try {
                    val color = jsonObject.getInt("color")
                    embedBuilder.setColor(color)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

            if (jsonObject.has("author")) {
                try {
                    val author = jsonObject.getJSONObject("author")
                    val name = author.getString("name")
                    val iconUrl = if (author.has("icon_url")) author.getString("icon_url") else null
                    embedBuilder.setAuthor(name, null, iconUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (jsonObject.has("thumbnail")) {
                try {
                    val thumbnail = jsonObject.getJSONObject("thumbnail")
                    if (thumbnail.has("url")) {
                        embedBuilder.setThumbnail(thumbnail.getString("url"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (jsonObject.has("image")) {
                try {
                    val image = jsonObject.getJSONObject("image")
                    if (image.has("url")) {
                        embedBuilder.setImage(image.getString("url"))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (jsonObject.has("footer")) {
                try {
                    val footer = jsonObject.getJSONObject("footer")
                    val text = footer.getString("text")
                    val iconUrl = if (footer.has("icon_url")) footer.getString("icon_url") else null
                    embedBuilder.setFooter(text, iconUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (jsonObject.has("fields")) {
                try {
                    val fields = jsonObject.getJSONArray("fields")
                    for (i in 0 until fields.length()) {
                        try {
                            val field = fields.getJSONObject(i)
                            val name = field.getString("name")
                            val value = field.getString("value")
                            val inline = if (field.has("inline")) field.getBoolean("inline") else false
                            embedBuilder.addField(name, value, inline)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            event.channel.sendMessageEmbeds(embedBuilder.build()).queue()
            event.reply("Embed criado com sucesso!").setEphemeral(true).queue()

        } catch (_: JSONException) {
            event.reply("JSON inválido. Por favor, verifique o formato e tente novamente.").setEphemeral(true).queue()
        } catch (e: Exception) {
            event.reply("Ocorreu um erro ao processar o embed: ${e.message}").setEphemeral(true).queue()
        }
    }
}
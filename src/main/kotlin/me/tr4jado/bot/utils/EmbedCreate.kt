package me.tr4jado.bot.utils

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

object EmbedCreate {
    fun createConfigEmbed(config: Map<String, Any>): EmbedBuilder {
        val embedBuilder = EmbedBuilder()

        (config["title"] as? String)?.let { embedBuilder.setTitle(it) }
        (config["description"] as? String)?.let { embedBuilder.setDescription(it) }
        (config["color"] as? Int)?.let { embedBuilder.setColor(Color(it)) }

        (config["image"] as? String)?.let { embedBuilder.setImage(it) }
        (config["thumbnail"] as? String)?.let { embedBuilder.setThumbnail(it) }

        when (val url = config["url"]) {
            is String -> if (url.matches(Regex("^https?://.+$"))) {
                embedBuilder.setUrl(url)
            }
        }

        when (val timestamp = config["timestamp"]) {
            is String -> embedBuilder.setTimestamp(java.time.Instant.parse(timestamp))
            is Map<*, *> -> {
                val timeString = timestamp["time"]?.toString()
                if (timeString != null) {
                    embedBuilder.setTimestamp(java.time.Instant.parse(timeString))
                }
            }
        }

        when (val fields = config["fields"]) {
            is List<*> -> {
                fields.forEach { field ->
                    if (field is Map<*, *>) {
                        val name = (field["name"] ?: "").toString()
                        val value = (field["value"] ?: "").toString()
                        val inline = (field["inline"]?.toString()?.toBoolean() ?: false)
                        embedBuilder.addField(name, value, inline)
                    }
                }
            }
        }

        when (val author = config["author"]) {
            is Map<*, *> -> {
                val name = author["name"] as? String

                when (val iconUrl = author["icon_url"]) {
                    is String -> if (iconUrl.matches(Regex("^https?://.+$"))) { embedBuilder.setAuthor(name, null, iconUrl) }
                    else -> embedBuilder.setAuthor(name)
                }
            }
        }

        when (val footer = config["footer"]) {
            is Map<*, *> -> {
                val text = (footer["text"] ?: "").toString()
                val iconUrl = footer["icon_url"]?.toString()
                if (iconUrl != null) {
                    embedBuilder.setFooter(text, iconUrl)
                } else {
                    embedBuilder.setFooter(text)
                }
            }
        }

        return embedBuilder
    }
}
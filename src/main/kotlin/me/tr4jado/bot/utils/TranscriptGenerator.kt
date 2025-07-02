package me.tr4jado.bot.utils

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TranscriptGenerator {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    }

    fun generateTranscriptFile(channel: TextChannel, messages: List<Message>): File {
        val fileName = "transcript_${channel.name}_${System.currentTimeMillis()}.html"
        val file = File(fileName)
        file.writeText(generateHtmlTranscript(channel, messages))
        return file
    }

    private fun generateHtmlTranscript(channel: TextChannel, messages: List<Message>): String {
        val html = StringBuilder()

        html.append("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Transcript - ${channel.name}</title>
            <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
            <style>
                :root {
                    --bg-primary: #1e1f22;
                    --bg-secondary: #2b2d31;
                    --bg-message: #313338;
                    --bg-message-hover: #383a40;
                    --text-primary: #dbdee1;
                    --text-secondary: #b5bac1;
                    --text-muted: #949ba4;
                    --accent: #5865f2;
                    --border: #3f4248;
                    --attachment: #00a8fc;
                    --embed-bg: #282b30;
                }
                
                body {
                    font-family: 'Inter', 'Roboto', sans-serif;
                    background-color: var(--bg-primary);
                    color: var(--text-primary);
                    margin: 0;
                    padding: 0;
                    line-height: 1.5;
                }
                
                .container {
                    max-width: 1000px;
                    margin: 0 auto;
                    padding: 2rem 1rem;
                }
                
                .header {
                    text-align: center;
                    margin-bottom: 2rem;
                    padding-bottom: 1.5rem;
                    border-bottom: 1px solid var(--border);
                }
                
                .header h1 {
                    font-weight: 600;
                    color: var(--text-primary);
                    margin-bottom: 0.5rem;
                    font-size: 1.8rem;
                }
                
                .header p {
                    color: var(--text-secondary);
                    margin: 0.3rem 0;
                    font-size: 0.95rem;
                }
                
                .messages-container {
                    display: flex;
                    flex-direction: column;
                    gap: 1rem;
                }
                
                .message {
                    background-color: var(--bg-message);
                    border-radius: 8px;
                    padding: 1rem;
                    transition: all 0.2s ease;
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
                }
                
                .message:hover {
                    background-color: var(--bg-message-hover);
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
                }
                
                .message-header {
                    display: flex;
                    align-items: center;
                    margin-bottom: 0.5rem;
                    gap: 0.5rem;
                }
                
                .author {
                    font-weight: 500;
                    color: var(--text-primary);
                    font-size: 1rem;
                }
                
                .author-bot {
                    background-color: var(--accent);
                    color: white;
                    font-size: 0.7rem;
                    padding: 0.15rem 0.4rem;
                    border-radius: 3px;
                    margin-left: 0.3rem;
                }
                
                .timestamp {
                    color: var(--text-muted);
                    font-size: 0.75rem;
                }
                
                .content {
                    margin-left: 0.5rem;
                    color: var(--text-primary);
                    font-size: 0.95rem;
                    white-space: pre-wrap;
                }
                
                .attachment {
                    margin-top: 0.5rem;
                    margin-left: 0.5rem;
                }
                
                .attachment a {
                    color: var(--attachment);
                    text-decoration: none;
                    font-size: 0.9rem;
                    display: inline-flex;
                    align-items: center;
                    gap: 0.3rem;
                }
                
                .attachment a:hover {
                    text-decoration: underline;
                }
                
                .attachment a::before {
                    content: "📎";
                }
                
                .embed {
                    background-color: var(--embed-bg);
                    border-left: 3px solid var(--border);
                    border-radius: 4px;
                    padding: 0.75rem;
                    margin-top: 0.75rem;
                    font-size: 0.85rem;
                }
                
                .embed-title {
                    font-weight: 500;
                    margin-bottom: 0.3rem;
                    color: var(--text-primary);
                }
                
                .embed-description {
                    color: var(--text-secondary);
                }
                
                .footer {
                    text-align: center;
                    margin-top: 2rem;
                    padding-top: 1.5rem;
                    border-top: 1px solid var(--border);
                    color: var(--text-muted);
                    font-size: 0.85rem;
                }
                
                @media (max-width: 768px) {
                    .container {
                        padding: 1rem 0.5rem;
                    }
                    
                    .header h1 {
                        font-size: 1.5rem;
                    }
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>#${channel.name}</h1>
                    <p>${messages.size} mensagens • ${LocalDateTime.now().format(DATE_FORMATTER)}</p>
                </div>
                
                <div class="messages-container">
        """.trimIndent())

        messages.forEach { message ->
            val isBot = message.author.isBot
            html.append("""
            <div class="message">
                <div class="message-header">
                    <span class="author">${message.author.name}</span>
                    ${if (isBot) """<span class="author-bot">BOT</span>""" else ""}
                    <span class="timestamp">${message.timeCreated.format(DATE_FORMATTER)}</span>
                </div>
                <div class="content">${message.contentRaw.escapeHtml()}</div>
        """.trimIndent())

            message.attachments.forEach { attachment ->
                html.append("""
                <div class="attachment">
                    <a href="${attachment.url}" target="_blank">${attachment.fileName}</a>
                </div>
            """.trimIndent())
            }

            message.embeds.forEach { embed ->
                html.append("""
                <div class="embed">
                    ${embed.title?.let { """<div class="embed-title">${it.escapeHtml()}</div>""" } ?: ""}
                    ${embed.description?.let { """<div class="embed-description">${it.escapeHtml()}</div>""" } ?: ""}
                </div>
            """.trimIndent())
            }

            html.append("</div>")
        }

        html.append("""
                </div>
                <div class="footer">
                    Transcript gerado automaticamente • ${LocalDateTime.now().year}
                </div>
            </div>
        </body>
        </html>
        """.trimIndent())

        return html.toString()
    }

    private fun String.escapeHtml(): String {
        return this.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
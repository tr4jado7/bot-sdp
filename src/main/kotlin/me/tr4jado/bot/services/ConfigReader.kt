package me.tr4jado.bot.services

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Paths

object ConfigReader {
    fun loadConfig(configFileName: String, absolutePath: String? = null): Map<String, Any> {
        val configFile = when {
            absolutePath != null -> File(absolutePath)
            else -> {
                val jarDir = Paths.get("").toAbsolutePath().toString()
                File(jarDir, configFileName)
            }
        }

        if (!configFile.exists()) {
            throw IllegalArgumentException("Arquivo de configuração não encontrado: ${configFile.absolutePath}")
        }

        return Yaml().load(configFile.inputStream())
    }

    fun loadConfigs(configFileNames: List<String>, absolutePaths: List<String?>? = null): Map<String, Any> {
        require(absolutePaths == null || configFileNames.size == absolutePaths.size) {
            "Se absolutePaths for fornecido, deve ter o mesmo tamanho que configFileNames"
        }

        val mergedConfig = mutableMapOf<String, Any>()

        configFileNames.forEachIndexed { index, configName ->
            val absolutePath = absolutePaths?.get(index)
            val currentConfig = loadConfig(configName, absolutePath)

            // Mescla os maps recursivamente
            mergeMaps(mergedConfig, currentConfig)
        }

        return mergedConfig
    }

    fun saveConfig(config: Map<String, Any>, configFileName: String, absolutePath: String? = null) {
        try {
            val configFile = when {
                absolutePath != null -> File(absolutePath)
                else -> {
                    val jarDir = Paths.get("").toAbsolutePath().toString()
                    File(jarDir, configFileName)
                }
            }

            // Garante que o diretório pai existe
            configFile.parentFile?.mkdirs()

            // Escreve as configurações no arquivo
            Yaml().dump(config, configFile.writer().buffered())
        } catch (e: Exception) {
            throw RuntimeException("Falha ao salvar configurações: ${e.message}", e)
        }
    }

    private fun mergeMaps(baseMap: MutableMap<String, Any>, newMap: Map<String, Any>) {
        newMap.forEach { (key, newValue) ->
            val existingValue = baseMap[key]

            when {
                existingValue is Map<*, *> && newValue is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val existingSubMap = existingValue as MutableMap<String, Any>
                    @Suppress("UNCHECKED_CAST")
                    val newSubMap = newValue as Map<String, Any>
                    mergeMaps(existingSubMap, newSubMap)
                }

                else -> baseMap[key] = newValue
            }
        }
    }
}
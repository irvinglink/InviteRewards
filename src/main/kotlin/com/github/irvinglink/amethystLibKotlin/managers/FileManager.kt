package com.github.irvinglink.amethystLibKotlin.managers

import com.github.irvinglink.amethystLibKotlin.files.files.ConfigFile
import com.github.irvinglink.amethystLibKotlin.files.files.LangFile
import com.github.irvinglink.amethystLibKotlin.files.YamlFile
import org.bukkit.plugin.java.JavaPlugin

class FileManager(plugin: JavaPlugin) {

    val configFile = ConfigFile(plugin)
    val langFile = LangFile(plugin)

    private val files: List<YamlFile> = listOf(
        configFile,
        langFile
    )

    fun loadAll() {
        files.forEach {
            it.createAndLoad()
            it.updateMissingKeys()
        }
    }

    fun reloadAll() {
        files.forEach {
            it.reload()
        }
    }

    fun reloadAllAndUpdate() {
        files.forEach {
            it.reload()
            it.updateMissingKeys()
        }
    }

    fun saveAll() {
        files.forEach {
            it.save()
        }
    }
}
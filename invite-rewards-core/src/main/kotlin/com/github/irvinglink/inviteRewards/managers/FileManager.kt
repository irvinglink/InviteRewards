package com.github.irvinglink.inviteRewards.managers

import com.github.irvinglink.inviteRewards.files.files.ConfigFile
import com.github.irvinglink.inviteRewards.files.files.LangFile
import com.github.irvinglink.inviteRewards.files.YamlFile
import com.github.irvinglink.inviteRewards.files.files.PendingRewardsMenuFile
import org.bukkit.plugin.java.JavaPlugin

class FileManager(plugin: JavaPlugin) {

    val configFile = ConfigFile(plugin)
    val langFile = LangFile(plugin)
    val pendingRewardsMenuFile = PendingRewardsMenuFile(plugin)

    private val files: List<YamlFile> = listOf(
        configFile,
        langFile,
        pendingRewardsMenuFile
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
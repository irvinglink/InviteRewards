package com.github.irvinglink.inviteRewards.storage.providers.yaml

import com.github.irvinglink.inviteRewards.files.YamlFile
import com.github.irvinglink.inviteRewards.storage.StorageConfig
import com.github.irvinglink.inviteRewards.storage.StorageProvider
import org.bukkit.plugin.java.JavaPlugin

class YamlStorage(
    private val plugin: JavaPlugin,
    private val config: StorageConfig
) : StorageProvider {

    val files = mutableMapOf<String, YamlFile>()

    override fun setup() {
        plugin.logger.info("YAML storage ready.")
    }

    fun getFile(name: String): YamlFile {
        return files.getOrPut(name) {
            YamlFile(plugin, "${config.yaml.folder}/$name.yml")
                .createAndLoad()
        }
    }

    override fun shutdown() {
        files.values.forEach { it.save() }
    }

    override fun isReady(): Boolean = true
}
package com.github.irvinglink.inviteRewards.files

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

open class YamlFile(
    private val plugin: JavaPlugin,
    val fileName: String
) {

    val file: File = File(plugin.dataFolder, fileName)

    var config: YamlConfiguration = YamlConfiguration()
        private set

    fun create(): YamlFile {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        file.parentFile?.takeIf { !it.exists() }?.mkdirs()

        if (!file.exists()) {
            plugin.getResource(fileName)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: file.createNewFile()
        }

        return this
    }

    fun load(): YamlFile {
        config = YamlConfiguration.loadConfiguration(file)
        return this
    }

    fun createAndLoad(): YamlFile {
        return create().load()
    }

    fun save() {
        config.save(file)
    }

    fun reload() {
        load()
    }

    fun exists(): Boolean {
        return file.exists()
    }

    fun updateMissingKeys(): Boolean {
        val resource = plugin.getResource(fileName) ?: return false

        val internal = YamlConfiguration.loadConfiguration(resource.reader(Charsets.UTF_8))
        var changed = false

        for (key in internal.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, internal.get(key))
                changed = true
            }
        }

        if (changed) {
            save()
        }

        return changed
    }

    fun getString(path: String): String? = config.getString(path)

    fun getString(path: String, default: String): String {
        return config.getString(path) ?: default
    }

    fun getInt(path: String): Int = config.getInt(path)

    fun getInt(path: String, default: Int): Int {
        return if (config.contains(path)) config.getInt(path) else default
    }

    fun getBoolean(path: String): Boolean = config.getBoolean(path)

    fun getBoolean(path: String, default: Boolean): Boolean {
        return if (config.contains(path)) config.getBoolean(path) else default
    }

    fun getDouble(path: String): Double = config.getDouble(path)

    fun getLong(path: String): Long = config.getLong(path)

    fun getStringList(path: String): List<String> = config.getStringList(path)

    fun contains(path: String): Boolean = config.contains(path)

    fun set(path: String, value: Any?) {
        config.set(path, value)
    }

    fun getConfigurationSection(string: String): ConfigurationSection? {
        return config.getConfigurationSection(string)
    }

}
package com.github.irvinglink.amethystLibKotlin.files.files

import com.github.irvinglink.amethystLibKotlin.files.YamlFile
import org.bukkit.plugin.java.JavaPlugin

class ConfigFile(plugin: JavaPlugin) : YamlFile(plugin, "config.yml")
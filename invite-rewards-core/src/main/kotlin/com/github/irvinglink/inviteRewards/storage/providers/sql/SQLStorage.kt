package com.github.irvinglink.inviteRewards.storage.providers.sql

import com.github.irvinglink.inviteRewards.storage.StorageConfig
import com.github.irvinglink.inviteRewards.storage.StorageProvider
import org.bukkit.plugin.java.JavaPlugin
import java.sql.Connection
import java.sql.DriverManager

abstract class SQLStorage(
    protected val plugin: JavaPlugin,
    protected val config: StorageConfig
) : StorageProvider {

    protected var rawConnection: Connection? = null

    val connection: Connection
        get() = rawConnection ?: error("Database not connected")

    override fun isReady(): Boolean {
        return rawConnection != null && !rawConnection!!.isClosed
    }

    override fun shutdown() {
        rawConnection?.close()
        rawConnection = null
    }

    protected fun connect(url: String, username: String?, password: String?) {
        rawConnection = if (username != null) {
            DriverManager.getConnection(url, username, password)
        } else {
            DriverManager.getConnection(url)
        }

        plugin.logger.info("Connected to database.")
    }
}
package com.github.irvinglink.amethystLibKotlin.hooks

import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player

object VaultHook {

    private val plugin get() = PluginContext.plugin
    private var economy: Economy? = null

    val isEnabled: Boolean get() = economy != null

    fun setup(): Boolean {
        if (!plugin.server.pluginManager.isPluginEnabled("Vault")) {
            plugin.logger.warning("Vault not found — economy features disabled.")
            return false
        }

        val rsp = plugin.server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            plugin.logger.warning("No economy provider found — economy features disabled.")
            return false
        }

        economy = rsp.provider
        plugin.logger.info("Vault hooked into: ${rsp.provider.name}")
        return true
    }

    fun getBalance(player: Player): Double {
        return economy?.getBalance(player) ?: 0.0
    }

    fun has(player: Player, amount: Double): Boolean {
        return economy?.has(player, amount) ?: false
    }

    fun withdraw(player: Player, amount: Double): Boolean {
        val eco = economy ?: return false
        if (!eco.has(player, amount)) return false
        return eco.withdrawPlayer(player, amount).transactionSuccess()
    }

    fun deposit(player: Player, amount: Double): Boolean {
        return economy?.depositPlayer(player, amount)?.transactionSuccess() ?: false
    }
}
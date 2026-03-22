package com.github.irvinglink.amethystLibKotlin.commands.builders

import com.github.irvinglink.amethystLibKotlin.AmethystLibKotlin
import com.github.irvinglink.amethystLibKotlin.core.PluginContext
import com.github.irvinglink.amethystLibKotlin.enums.config.MESSAGES
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderContext
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

abstract class BaseCommand(
    private val commandName: String,
    private val permission: String? = null,
    private val allowConsole: Boolean = true,
) : CommandExecutor, TabCompleter {

    protected val plugin: AmethystLibKotlin
        get() = PluginContext.plugin

    protected val chat
        get() = plugin.chat

    private val subCommands = mutableListOf<SubCommand>()

    init {
        val pluginCommand: PluginCommand? = plugin.getCommand(commandName)

        if (pluginCommand == null) {
            plugin.logger.warning("Command '$commandName' not found in plugin.yml")
        } else {
            pluginCommand.setExecutor(this)
        }
    }

    protected fun registerSubCommand(subCommand: SubCommand) {
        subCommands += subCommand
    }

    protected fun registerSubCommands(vararg subCommands: SubCommand) {
        this.subCommands += subCommands
    }

    protected fun getSubCommands(): List<SubCommand> = subCommands.toList()

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>,
    ): Boolean {
        if (!hasPermission(sender, permission)) {
            sendNoPermission(sender)
            return true
        }

        if (!allowConsole && sender !is Player) {
            sendNoConsole(sender)
            return true
        }

        if (args.isEmpty()) {
            execute(sender, args)
            return true
        }

        val subCommand = findSubCommand(args[0])

        if (subCommand == null) {
            execute(sender, args)
            return true
        }

        if (!hasPermission(sender, subCommand.permission)) {
            sendNoPermission(sender)
            return true
        }

        if (!subCommand.allowConsole && sender !is Player) {
            sendNoConsole(sender)
            return true
        }

        subCommand.execute(sender, args.drop(1).toTypedArray())
        return true
    }

    private fun findSubCommand(input: String): SubCommand? {
        return subCommands.firstOrNull { sub ->
            sub.name.equals(input, ignoreCase = true) ||
                    sub.aliases.any { alias -> alias.equals(input, ignoreCase = true) }
        }
    }

    private fun hasPermission(sender: CommandSender, permission: String?): Boolean {
        return permission.isNullOrBlank() || sender.hasPermission(permission)
    }

    protected fun sendNoPermission(sender: CommandSender) {
        sendMessage(sender, MESSAGES.NO_PERMISSION)
    }

    protected fun sendNoConsole(sender: CommandSender) {
        sendMessage(sender, MESSAGES.NO_PERMISSION_CONSOLE)
    }

    protected fun sendWrongUsage(sender: CommandSender, syntax: String) {
        val context = buildContext(sender, syntax)
        sender.sendMessage(chat.format(MESSAGES.WRONG_USAGE, context).orEmpty())
    }

    protected fun sendNotEnoughArgs(sender: CommandSender, syntax: String? = null) {
        val context = buildContext(sender, syntax)
        sender.sendMessage(chat.format(MESSAGES.NOT_ENOUGH_ARGS, context).orEmpty())
    }

    protected fun sendMessage(
        sender: CommandSender,
        message: MESSAGES,
        value: String? = null,
    ) {
        val context = buildContext(sender, value)
        sender.sendMessage(chat.format(message, context).orEmpty())
    }

    protected fun sendSubCommandHelp(sender: CommandSender) {
        subCommands.forEach { sub ->
            val line = "&8- &a/$commandName ${sub.syntax} &7- ${sub.description}"
            sender.sendMessage(chat.format(line).orEmpty())
        }
    }

    private fun buildContext(sender: CommandSender, value: String? = null): PlaceholderContext {
        return if (sender is Player) {
            PlaceholderContext(player = sender, value = value)
        } else {
            PlaceholderContext(value = value)
        }
    }

    protected abstract fun execute(sender: CommandSender, args: Array<String>)

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (!hasPermission(sender, permission)) {
            return emptyList()
        }

        if (!allowConsole && sender !is Player) {
            return emptyList()
        }

        if (args.size == 1) {
            return filterCompletions(
                args[0],
                subCommands
                    .filter { hasPermission(sender, it.permission) }
                    .filter { it.allowConsole || sender is Player }
                    .flatMap { listOf(it.name) + it.aliases }
                    .distinct()
            )
        }

        val subCommand = findSubCommand(args[0]) ?: return tabComplete(sender, args)

        if (!hasPermission(sender, subCommand.permission)) {
            return emptyList()
        }

        if (!subCommand.allowConsole && sender !is Player) {
            return emptyList()
        }

        return filterCompletions(
            args.last(),
            subCommand.tabComplete(sender, args.drop(1).toTypedArray())
        )
    }

    protected open fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }

    protected fun filterCompletions(input: String, options: List<String>): List<String> {
        return options
            .filter { it.startsWith(input, ignoreCase = true) }
            .sorted()
    }


}
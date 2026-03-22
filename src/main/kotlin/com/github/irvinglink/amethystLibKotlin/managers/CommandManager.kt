package com.github.irvinglink.amethystLibKotlin.managers

import com.github.irvinglink.amethystLibKotlin.commands.builders.BaseCommand
import com.github.irvinglink.amethystLibKotlin.commands.commands.MainCommand

class CommandManager {

    private val commands = mutableListOf<BaseCommand>()

    init {
        registerAll(
            MainCommand()
        )
    }

    fun register(command: BaseCommand) {
        commands += command
    }

    fun registerAll(vararg commands: BaseCommand) {
        this.commands += commands
    }

    fun getCommands(): List<BaseCommand> {
        return commands.toList()
    }

    fun size(): Int {
        return commands.size
    }

    fun isEmpty(): Boolean {
        return commands.isEmpty()
    }
}
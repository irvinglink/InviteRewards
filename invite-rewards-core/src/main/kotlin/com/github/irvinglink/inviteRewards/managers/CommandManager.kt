package com.github.irvinglink.inviteRewards.managers

import com.github.irvinglink.inviteRewards.commands.builders.BaseCommand
import com.github.irvinglink.inviteRewards.commands.commands.InvitationRewardsCommand
import com.github.irvinglink.inviteRewards.commands.commands.InviteRewardsCommand

class CommandManager {

    private val commands = mutableListOf<BaseCommand>()

    init {
        registerAll(
            InviteRewardsCommand(),
            InvitationRewardsCommand()
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
package com.github.irvinglink.inviteRewards.models.action

import com.github.irvinglink.inviteRewards.core.PluginContext
import com.github.irvinglink.inviteRewards.utils.nms.PacketUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class ExecutableAction private constructor(
    private val instructions: List<ActionInstruction>
) {

    companion object {
        var handler: ActionHandler = LiteActionHandler()

        fun fromInstruction(rawInstruction: String): ExecutableAction {
            val instruction = ActionInstructionParser.parse(rawInstruction)
            return ExecutableAction(instruction?.let(::listOf) ?: emptyList())
        }

        fun fromInstructions(rawInstructions: List<String>): ExecutableAction {
            return ExecutableAction(ActionInstructionParser.parseAll(rawInstructions))
        }

        fun of(instructions: List<ActionInstruction>): ExecutableAction {
            return ExecutableAction(instructions)
        }
    }

    private val plugin = PluginContext.plugin
    private val chat = plugin.chat

    fun execute(player: Player, target: OfflinePlayer? = null) {
        instructions.forEach { instruction ->
            if (handler.handle(player, target, instruction)) return@forEach

            when (instruction) {
                is ActionInstruction.Message -> {
                    if (player.isOnline) player.sendMessage(replace(player, target, instruction.text))
                }

                is ActionInstruction.Console -> {
                    runSync {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replace(player, target, instruction.command))
                    }
                }

                is ActionInstruction.PlayerCommand -> {
                    if (player.isOnline) runSync { player.performCommand(replace(player, target, instruction.command)) }
                }

                is ActionInstruction.Sound -> {
                    if (player.isOnline) PacketUtils.playSound(player, instruction.sound, instruction.volume, instruction.pitch)
                }

                else -> {}
            }
        }
    }

    private fun replace(player: Player, target: OfflinePlayer?, text: String): String {
        return chat.format(player, target, null, text, true)
    }

    private fun runSync(block: () -> Unit) {
        if (Bukkit.isPrimaryThread()) block()
        else Bukkit.getScheduler().runTask(plugin, Runnable { block() })
    }
}
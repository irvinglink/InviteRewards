package com.github.irvinglink.inviteRewards.action

import com.github.irvinglink.inviteRewards.features.guis.manager.MenuManager
import com.github.irvinglink.inviteRewards.models.action.ActionHandler
import com.github.irvinglink.inviteRewards.models.action.ActionInstruction
import com.github.irvinglink.inviteRewards.utils.nms.PacketUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class PremiumActionHandler : ActionHandler {

    override fun handle(player: Player, target: OfflinePlayer?, instruction: ActionInstruction): Boolean {
        if (!player.isOnline) return false

        return when (instruction) {
            is ActionInstruction.Title -> {
                runSync {
                    PacketUtils.sendTitle(
                        player = player,
                        title = replace(player, target, instruction.title),
                        subtitle = instruction.subtitle?.let { replace(player, target, it) },
                        fadeIn = instruction.fadeIn,
                        stay = instruction.stay,
                        fadeOut = instruction.fadeOut
                    )
                }
                true
            }

            is ActionInstruction.ActionBar -> {
                PacketUtils.sendActionBar(player, replace(player, target, instruction.text))
                true
            }

            is ActionInstruction.OpenMenu -> {
                runSync { MenuManager.openMenu(player, instruction.menuId) }
                true
            }

            is ActionInstruction.CloseMenu -> {
                runSync { MenuManager.closeMenu(player) }
                true
            }

            else -> false
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
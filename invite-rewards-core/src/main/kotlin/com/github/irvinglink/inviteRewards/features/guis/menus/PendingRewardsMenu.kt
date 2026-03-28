package com.github.irvinglink.inviteRewards.features.guis.menus

import com.github.irvinglink.inviteRewards.core.PluginContext.plugin
import com.github.irvinglink.inviteRewards.database.models.PendingReward
import com.github.irvinglink.inviteRewards.database.models.RewardType
import com.github.irvinglink.inviteRewards.features.guis.manager.Menu
import com.github.irvinglink.inviteRewards.features.guis.manager.MenuManager
import com.github.irvinglink.inviteRewards.features.guis.models.ItemMenu
import com.github.irvinglink.inviteRewards.features.guis.models.MenuClickType
import com.github.irvinglink.inviteRewards.models.action.ExecutableAction
import com.github.irvinglink.inviteRewards.utils.items.CustomItems
import com.github.irvinglink.inviteRewards.utils.nms.PacketUtils
import com.github.irvinglink.inviteRewards.utils.paginator.Paginator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import java.util.concurrent.CompletableFuture

class PendingRewardsMenu(
    player: Player,
    private val rewards: List<PendingReward>,
    private val page: Int = 1,
) : Menu(
    id = "pending_rewards_$page",
    title = plugin.pendingRewardsMenuFile.getString("menu.title") ?: "&d&lPending &e&lRewards",
    rows = 6,
    enableSlots = false
) {

    companion object {
        private const val PER_PAGE = 28
        private val REWARD_SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        )
    }

    init {
        val paginator = Paginator<PendingReward>()
        val result = paginator.paginate(page, PER_PAGE, rewards)

        fillBorder(
            buildConfiguredItem(
                path = "items.border",
                defaultMaterial = Material.GRAY_STAINED_GLASS_PANE,
                replacements = emptyMap(),
                hideAttributes = true
            )
        )

        result.items.forEachIndexed { index, reward ->
            val slot = REWARD_SLOTS.getOrNull(index) ?: return@forEachIndexed

            val claimer = reward.claimerUuid?.let(plugin.server::getOfflinePlayer)
            val inviter = reward.inviterUuid?.let(plugin.server::getOfflinePlayer)
            val relatedName = claimer?.name ?: inviter?.name ?: "Unknown"

            val rewardKey = getRewardTypeKey(reward.rewardType)

            addItems(
                ItemMenu(
                    id = "reward_${page}_$index",
                    itemStack = buildConfiguredItem(
                        path = "items.$rewardKey",
                        defaultMaterial = rewardMaterial(reward.rewardType),
                        replacements = mapOf(
                            "%claimer%" to relatedName,
                            "%inviter%" to (inviter?.name ?: "Unknown"),
                            "%related_player%" to relatedName,
                            "%reward_type%" to rewardLabel(reward),
                            "%invite_type_id%" to reward.inviteTypeId,
                            "%created_at%" to formatTime(reward.createdAt),
                            "%claimed_at%" to formatTime(reward.createdAt),
                            "%tier%" to (reward.tier?.toString() ?: "0"),
                            "%total_rewards%" to rewards.size.toString(),
                            "%current_page%" to result.currentPage.toString(),
                            "%total_pages%" to result.totalPages.toString(),
                            "%target_page%" to result.currentPage.toString()
                        )
                    ),
                    slot = slot,
                    onClick = { clickPlayer, _ ->
                        playConfiguredSound(clickPlayer, "items.$rewardKey")
                        MenuManager.closeMenu(clickPlayer)

                        plugin.rewardManager.deliverPendingReward(clickPlayer, reward)
                            .thenCompose {
                                plugin.database.pendingRewards.loadAll(clickPlayer.uniqueId)
                            }
                            .thenAccept { updatedRewards ->
                                plugin.server.scheduler.runTask(plugin, Runnable {
                                    val safePage = when {
                                        updatedRewards.isEmpty() -> 1
                                        else -> {
                                            val totalPages = ((updatedRewards.size - 1) / PER_PAGE) + 1
                                            page.coerceAtMost(totalPages)
                                        }
                                    }

                                    MenuManager.openMenu(
                                        clickPlayer,
                                        PendingRewardsMenu(clickPlayer, updatedRewards, safePage)
                                    )
                                })
                            }
                    }
                )
            )
        }

        if (rewards.isNotEmpty()) {
            addItems(
                ItemMenu(
                    id = "claim_all",
                    itemStack = buildConfiguredItem(
                        path = "items.claim_all",
                        defaultMaterial = Material.CHEST,
                        replacements = mapOf(
                            "%total_rewards%" to rewards.size.toString(),
                            "%current_page%" to result.currentPage.toString(),
                            "%total_pages%" to result.totalPages.toString(),
                            "%target_page%" to result.currentPage.toString()
                        )
                    ),
                    slot = 45,
                    onClick = { clickPlayer, _ ->
                        playConfiguredSound(clickPlayer, "items.claim_all")
                        MenuManager.closeMenu(clickPlayer)

                        val futures = rewards.map { reward ->
                            plugin.rewardManager.deliverPendingReward(clickPlayer, reward)
                        }

                        CompletableFuture.allOf(*futures.toTypedArray())
                            .thenCompose {
                                plugin.database.pendingRewards.loadAll(clickPlayer.uniqueId)
                            }
                            .thenAccept { updatedRewards ->
                                plugin.server.scheduler.runTask(plugin, Runnable {
                                    val safePage = when {
                                        updatedRewards.isEmpty() -> 1
                                        else -> {
                                            val totalPages = ((updatedRewards.size - 1) / PER_PAGE) + 1
                                            page.coerceAtMost(totalPages)
                                        }
                                    }

                                    MenuManager.openMenu(
                                        clickPlayer,
                                        PendingRewardsMenu(clickPlayer, updatedRewards, safePage)
                                    )
                                })
                            }
                    }
                )
            )
        }

        if (result.hasPrevious) {
            val soundInst = plugin.pendingRewardsMenuFile.getString("items.previous.sound")?.let { "[sound] $it" } ?: ""
            addItems(
                ItemMenu(
                    id = "previous",
                    itemStack = buildConfiguredItem(
                        path = "items.previous",
                        defaultMaterial = Material.ARROW,
                        replacements = mapOf(
                            "%target_page%" to (result.currentPage - 1).toString(),
                            "%current_page%" to result.currentPage.toString(),
                            "%total_pages%" to result.totalPages.toString(),
                            "%total_rewards%" to rewards.size.toString()
                        )
                    ),
                    slot = 48,
                    actions = mapOf(
                        MenuClickType.DEFAULT to ExecutableAction.fromInstructions(
                            listOfNotNull("[open_menu] pending_rewards_${page - 1}", soundInst.ifBlank { null })
                        )
                    )
                )
            )
        }

        addItems(
            ItemMenu(
                id = "page_info",
                itemStack = buildConfiguredItem(
                    path = "items.page_info",
                    defaultMaterial = Material.PAPER,
                    replacements = mapOf(
                        "%current_page%" to result.currentPage.toString(),
                        "%total_pages%" to result.totalPages.toString(),
                        "%total_rewards%" to rewards.size.toString(),
                        "%target_page%" to result.currentPage.toString()
                    )
                ),
                slot = 49
            )
        )

        if (result.hasNext) {
            val soundInst = plugin.pendingRewardsMenuFile.getString("items.next.sound")?.let { "[sound] $it" } ?: ""
            addItems(
                ItemMenu(
                    id = "next",
                    itemStack = buildConfiguredItem(
                        path = "items.next",
                        defaultMaterial = Material.ARROW,
                        replacements = mapOf(
                            "%target_page%" to (result.currentPage + 1).toString(),
                            "%current_page%" to result.currentPage.toString(),
                            "%total_pages%" to result.totalPages.toString(),
                            "%total_rewards%" to rewards.size.toString()
                        )
                    ),
                    slot = 50,
                    actions = mapOf(
                        MenuClickType.DEFAULT to ExecutableAction.fromInstructions(
                            listOfNotNull("[open_menu] pending_rewards_${page + 1}", soundInst.ifBlank { null })
                        )
                    )
                )
            )
        }

        addItems(
            ItemMenu(
                id = "close",
                itemStack = buildConfiguredItem(
                    path = "items.close",
                    defaultMaterial = Material.BARRIER,
                    replacements = emptyMap(),
                    hideAttributes = true
                ),
                slot = 53,
                actions = mapOf(
                    MenuClickType.DEFAULT to ExecutableAction.fromInstructions(
                        listOfNotNull(
                            "[close_menu]",
                            plugin.pendingRewardsMenuFile.getString("items.close.sound")?.let { "[sound] $it" }
                        )
                    )
                ),
                onClick = {clickPlayer, _ ->
                    MenuManager.closeMenu(clickPlayer)
                }
            )
        )

        if (rewards.isEmpty()) {
            addItems(
                ItemMenu(
                    id = "empty",
                    itemStack = buildConfiguredItem(
                        path = "items.empty",
                        defaultMaterial = Material.GRAY_DYE,
                        replacements = emptyMap()
                    ),
                    slot = 22
                )
            )
        }
    }

    private fun playConfiguredSound(player: Player, path: String) {
        val rawSound = plugin.pendingRewardsMenuFile.getString("$path.sound") ?: return
        val instruction = if (rawSound.startsWith("[")) rawSound else "[sound] $rawSound"
        ExecutableAction.fromInstruction(instruction).execute(player)
    }

    private fun buildConfiguredItem(
        path: String,
        defaultMaterial: Material,
        replacements: Map<String, String>,
        hideAttributes: Boolean = false
    ) = CustomItems.builder(getMaterial("$path.material", defaultMaterial)).apply {
        name(applyPlaceholders(plugin.pendingRewardsMenuFile.getString("$path.name") ?: "&r", replacements))

        val lore = plugin.pendingRewardsMenuFile.getStringList("$path.lore")
            .map { applyPlaceholders(it, replacements) }

        if (lore.isNotEmpty()) {
            lore(*lore.toTypedArray())
        }

        if (hideAttributes) {
            flags(ItemFlag.HIDE_ATTRIBUTES)
        }
    }.build()

    private fun getMaterial(path: String, default: Material): Material {
        val materialName = plugin.pendingRewardsMenuFile.getString(path) ?: return default
        return Material.matchMaterial(materialName) ?: default
    }

    private fun applyPlaceholders(text: String, replacements: Map<String, String>): String {
        var result = text
        replacements.forEach { (key, value) ->
            result = result.replace(key, value)
        }
        return result
    }

    private fun getRewardTypeKey(type: RewardType): String {
        return type.name.lowercase()
    }

    private fun rewardMaterial(type: RewardType): Material = when (type) {
        RewardType.INVITER_CLAIM -> Material.EMERALD
        RewardType.CLAIMER_CLAIM -> Material.DIAMOND
        RewardType.INVITER_MILESTONE -> Material.NETHER_STAR
    }

    private fun rewardLabel(reward: PendingReward): String {
        val key = getRewardTypeKey(reward.rewardType)
        val format = plugin.pendingRewardsMenuFile.getString("reward-types.$key")
            ?: reward.rewardType.name

        return applyPlaceholders(
            format,
            mapOf(
                "%tier%" to (reward.tier?.toString() ?: "0"),
                "%invite_type_id%" to reward.inviteTypeId
            )
        )
    }

    private fun formatTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        val value = when {
            days > 0 -> days.toString()
            hours > 0 -> hours.toString()
            minutes > 0 -> minutes.toString()
            else -> ""
        }

        val path = when {
            days > 0 -> "time-format.days"
            hours > 0 -> "time-format.hours"
            minutes > 0 -> "time-format.minutes"
            else -> "time-format.just_now"
        }

        val format = plugin.pendingRewardsMenuFile.getString(path) ?: "Just now"
        return applyPlaceholders(format, mapOf("%value%" to value))
    }
}
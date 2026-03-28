package com.github.irvinglink.inviteRewards.features.guis.loader

import com.github.irvinglink.inviteRewards.features.guis.menus.PendingRewardsMenu

internal object DefaultMenusLoader {
    fun register() {
        MenuLoader.register("pending_rewards_menu") { player -> PendingRewardsMenu(player, listOf(), 1) }
    }
}
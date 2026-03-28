package com.github.irvinglink.inviteRewards

import com.github.irvinglink.inviteRewards.core.PluginContext

class InviteRewardsFree : InviteRewardsPlugin() {

    override val isPremium: Boolean = false

    override fun onEnable() {
        PluginContext.plugin = this
        super.onEnable()
        logger.info("InviteRewards Free (Lite) has been enabled!")
    }
}
package com.github.irvinglink.inviteRewards

import com.github.irvinglink.inviteRewards.action.PremiumActionHandler
import com.github.irvinglink.inviteRewards.commands.commands.InvitationRewardsCommand
import com.github.irvinglink.inviteRewards.commands.commands.InviteRewardsCommand
import com.github.irvinglink.inviteRewards.commands.subCommands.InviteRewardsCodeSubCommand
import com.github.irvinglink.inviteRewards.commands.subCommands.LeaderboardSubCommand
import com.github.irvinglink.inviteRewards.database.Database
import com.github.irvinglink.inviteRewards.leaderboard.LeaderboardManager
import com.github.irvinglink.inviteRewards.managers.leaderboard.ILeaderboardManager
import com.github.irvinglink.inviteRewards.models.action.ExecutableAction
import com.github.irvinglink.inviteRewards.storage.PremiumStorageManager
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderRegistry
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.providers.LeaderboardPlaceholderProvider
import com.github.irvinglink.inviteRewards.validators.PremiumInviteValidator

class InviteRewardsPremium : InviteRewardsPlugin() {

    override val isPremium: Boolean = true

    override var leaderboardManager: ILeaderboardManager? = null

    override fun setupPremiumFeatures() {

        // 1. STORAGE & DATABASE (MySQL / MariaDB)
        val premiumStorage = PremiumStorageManager(this, loadStorageConfig())
        premiumStorage.setup()
        this.storageManager = premiumStorage

        this.database = Database(this.storageManager.provider)

        // 2. LEADERBOARD SYSTEM
        val lb = LeaderboardManager()
        this.leaderboardManager = lb

        lb.refreshCache().thenRun {
            lb.startUpdateTask()
        }

        // 3. REGISTER LEADERBOARD PLACEHOLDER PROVIDER
        placeholderRegistry.registerProvider(LeaderboardPlaceholderProvider())

        // 4. VALIDATOR INJECTION (Unlimited invites & milestones)
        this.inviteManager.validator = PremiumInviteValidator()

        // 5. EXECUTABLE ACTIONS
        ExecutableAction.handler = PremiumActionHandler()

        // 6. COMMAND INJECTION
        val invitationCommand = getCommand("invitation")?.executor
        if (invitationCommand is InvitationRewardsCommand) {
            invitationCommand.registerSubCommand(LeaderboardSubCommand())
        }

        val inviteRewardsCommand = getCommand("inviterewards")?.executor
        if (inviteRewardsCommand is InviteRewardsCommand) {
            inviteRewardsCommand.registerSubCommand(InviteRewardsCodeSubCommand())
        }

        logger.info("InviteRewards Premium features have been fully loaded.")
    }

    override fun onDisable() {
        leaderboardManager?.stopUpdateTask()
        super.onDisable()
    }
}
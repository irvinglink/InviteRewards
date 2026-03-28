package com.github.irvinglink.inviteRewards.enums.config

import com.github.irvinglink.inviteRewards.core.PluginContext

enum class MESSAGES(
    val path: String,
    val defaultMessage: String
) {

    PREFIX("Prefix", "&8[&eInvite&dRewards&8] "),
    COMMAND_NO_EXISTS("Command_No_Exists", "%inviterewards_prefix% &cThat command does not exist&7."),
    NO_PERMISSION("No_Permission", "%inviterewards_prefix% &cYou do not have permission to use this command&7."),
    NO_PERMISSION_CONSOLE("No_Permission_Console", "%inviterewards_prefix% &cThis action cannot be performed via console&7."),
    WRONG_USAGE("Wrong_Usage", "%inviterewards_prefix% &cWrong usage. &b%inviterewards_command_syntax%"),
    NOT_ENOUGH_ARGS("Not_Enough_Arguments", "%inviterewards_prefix% &cNot enough arguments. &b%inviterewards_command_syntax%"),
    NOT_USE_SYMBOLS("Not_Use_Symbols", "%inviterewards_prefix% &cYou cannot use symbols&7."),
    RELOAD_FILES("Reload_Files", "%inviterewards_prefix% &aConfiguration files have been reloaded&7."),
    RELOAD_FILE("Reload_File", "%inviterewards_prefix% &aConfiguration file &b%inviterewards_file_name% &ahas been reloaded&7."),
    RELOAD_MENU("Reload_Menu", "%inviterewards_prefix% &aMenu configuration has been reloaded&7."),
    RELOAD_MENUS("Reload_Menus", "%inviterewards_prefix% &aMenu configuration have been reloaded&7."),
    MENU_NO_EXISTS("Menu_No_Exists", "%inviterewards_prefix% &cThat menu does not exist&7."),
    ONLY_NUMBERS("Only_Numbers", "%inviterewards_prefix% &cOnly numbers are allowed&7."),
    NO_COLORS("No_Colors", "%inviterewards_prefix% &cYou cannot use colors&7."),
    PLAYER_NOT_FOUND("Player_Not_Found", "%inviterewards_prefix% &cPlayer not found&7."),
    PLAYER_NOT_ONLINE("Player_Not_Online", "%inviterewards_prefix% &cPlayer is not online&7."),
    INVITE_CODE_MESSAGE("Invite.Code_Message","%inviterewards_prefix% &7Your invite code for &e%invite_type_id% &7is &b%code%&7. Share it with your friends!"),
    INVITE_CODE_HOVER("Invite.Code_Hover", "&eClick to copy &7(Type: &f%invite_type_id%&7)"),
    INVITE_NOTIFICATIONS_ENABLED("Invite.Notifications_Enabled", "%inviterewards_prefix% &7Join notifications: &aEnabled"),
    INVITE_NOTIFICATIONS_DISABLED("Invite.Notifications_Disabled", "%inviterewards_prefix% &7Join notifications: &cDisabled"),
    INVITE_CLAIM_SUCCESS_CLAIMER("Invite.Claim.Success_Claimer","%inviterewards_prefix% &aYou joined via &e%inviterewards_target_name%&a's invite!"),
    INVITE_CLAIM_SUCCESS_INVITER("Invite.Claim.Success_Inviter","%inviterewards_prefix% &e%inviterewards_target_name% &ajoined using your invite code!"),
    INVITE_CLAIM_INVALID_CODE("Invite.Claim.Invalid_Code","%inviterewards_prefix% &cThat invite code does not exist&7."),
    INVITE_CLAIM_ALREADY_CLAIMED("Invite.Claim.Already_Claimed","%inviterewards_prefix% &cYou have already claimed an invitation&7."),
    INVITE_CLAIM_SELF_INVITE("Invite.Claim.Self_Invite","%inviterewards_prefix% &cYou cannot use your own invite code&7."),
    INVITE_CLAIM_NOT_REGISTERED(   "Invite.Claim.Not_Registered","%inviterewards_prefix% &cYou are not registered yet&7."),
    INVITE_CLAIM_SUSPICIOUS("Invite.Claim.Suspicious","%inviterewards_prefix% &cYour invitation could not be processed&7."),
    INVITE_NO_PENDING_REWARDS("Invite.No_Pending_Rewards","%inviterewards_prefix% &7You have no pending rewards."),
    LEADERBOARD_HEADER("Invite.Leaderboard.Header","%inviterewards_prefix% &7--- &eGlobal Leaderboard &7---"),
    LEADERBOARD_ENTRY("Invite.Leaderboard.Entry","&8#%inviterewards_value% &e%inviterewards_player_name% &7— &e%inviterewards_player_total_invites% invites &7(&e%inviterewards_player_points% points&7)"),
    LEADERBOARD_EMPTY("Invite.Leaderboard.Empty","%inviterewards_prefix% &cNo leaderboard data available yet&7."),
    LEADERBOARD_PLAYER_RANK("Invite.Leaderboard.Player_Rank", "%inviterewards_prefix% &7Your current rank: &a#%inviterewards_player_leaderboard_rank%"),
    EDITOR_SUGGEST_MESSAGE("Editor.Suggest_Message","%inviterewards_prefix% &7Type &bexit&7 to stop editing."),
    EDITOR_EDITED_MESSAGE("Editor.Edited_Message","%inviterewards_prefix% &aThe message has been successfully edited&7."),
    EDITOR_EDITING_TITLE("Editor.Title.Editing_Title","&a&lEditing..."),
    EDITOR_EDITING_SUBTITLE("Editor.Title.Editing_SubTitle","&7Type &bexit &7to stop editing."),
    EDITOR_DONE_TITLE("Editor.Title.Done_Title","&a&lDone!");

    val message: String
        get() {
            val pluginInstance = PluginContext.plugin
            val value = pluginInstance.langFile.getString(path)
            return if (value == null || value == "null") defaultMessage else value
        }

}
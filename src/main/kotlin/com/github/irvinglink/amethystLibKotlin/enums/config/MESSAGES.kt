package com.github.irvinglink.amethystLibKotlin.enums.config

import com.github.irvinglink.amethystLibKotlin.core.PluginContext

enum class MESSAGES(
    val path: String,
    val defaultMessage: String
) {

    PREFIX("prefix", "&8[&5Amethyst&dLib&8] "),
    COMMAND_NO_EXISTS("Command_No_Exists", "%amethystlib_prefix% &cThat command does not exists&7."),
    NO_PERMISSION("No_Permission", "%amethystlib_prefix% &cYou do not have permission to use this command."),
    NO_PERMISSION_CONSOLE("No_Permission_Console", "%amethystlib_prefix% &cThis action can not be performed via console&7."),
    WRONG_USAGE("Wrong_Usage", "%amethystlib_prefix% &cWrong usage. &b%amethystlib_command_syntax%"),
    NOT_ENOUGH_ARGS("Not_Enough_Arguments", "%amethystlib_prefix% &cNot enough arguments. %amethystlib_command_syntax%"),
    NOT_USE_SYMBOLS("Not_Use_Symbols", "%amethystlib_prefix% &cYou can not use symbols&7."),
    RELOAD_FILES("Reload_Files", "%amethystlib_prefix% &aConfiguration files had been reloaded&7."),
    RELOAD_FILE("Reload_File", "%amethystlib_prefix% &aConfiguration file %amethystlib_file_name% has been reloaded&7."),
    RELOAD_MENU("Reload_Menu", "%amethystlib_prefix% &aMenu configuration had been reloaded&7."),
    RELOAD_MENUS("Reload_Menus", "%amethystlib_prefix% &aMenus configurations had been reloaded&7."),
    MENU_NO_EXISTS("Menu_No_Exists", "%amethystlib_prefix% &cThat menu does not exists&7."),
    ONLY_NUMBERS("Only_Numbers", "%amethystlib_prefix% &cOnly numbers are allowed&7."),
    NO_COLORS("No_Colors", "%amethystlib_prefix% &cYou can not use colors&7."),
    PLAYER_NOT_FOUND("Player_Not_Found", "%amethystlib_prefix% &cPlayer not found&7."),
    PLAYER_NOT_ONLINE("Player_Not_Online", "%amethystlib_prefix% &cPlayer is not online&7."),

    // EDITOR
    EDITOR_SUGGEST_MESSAGE("Editor.Suggest_Message", "%amethystlib_prefix% &7Type &bexit&7 to stop editing."),
    EDITOR_EDITED_MESSAGE("Editor.Edited_Message", "%amethystlib_prefix% &aThe message has been successfully edited&7."),
    EDITOR_EDITING_TITLE("Editor.Title.Editing_Title", "&a&lEditing..."),
    EDITOR_EDITING_SUBTITLE("Editor.Title.Editing_SubTitle", "&7Type &bexit &7to stop editing."),
    EDITOR_DONE_TITLE("Editor.Title.Done_Title", "&a&lDone!");

    val message: String
        get() {
            val value = plugin.langFile.getString(path) // CHANGE TO LANG FILE
            return (if (value == null || value == "null") defaultMessage else value) as String
        }

    companion object {
        private val plugin = PluginContext.plugin
    }
}
package com.github.irvinglink.inviteRewards.models.action

object ActionInstructionParser {

    fun parse(line: String): ActionInstruction? {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return null

        val split = trimmed.split(" ", limit = 2)
        val type = split[0].lowercase()
        val argument = split.getOrNull(1).orEmpty().trim()

        return when (type) {
            "[message]" -> ActionInstruction.Message(argument)
            "[console]" -> ActionInstruction.Console(argument)
            "[player]" -> ActionInstruction.PlayerCommand(argument)
            "[sound]" -> parseSound(argument)
            "[close_menu]" -> ActionInstruction.CloseMenu
            "[title]" -> parseTitle(argument)
            "[action_bar]" -> ActionInstruction.ActionBar(argument)
            "[open_menu]" -> ActionInstruction.OpenMenu(argument)
            else -> null
        }
    }

    fun parseAll(lines: List<String>): List<ActionInstruction> = lines.mapNotNull(::parse)

    private fun parseTitle(argument: String): ActionInstruction.Title {
        val titleParts = argument.split("subtitle:", limit = 2)
        val title = titleParts.getOrNull(0).orEmpty().trim()
        val subtitle = titleParts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        return ActionInstruction.Title(title = title, subtitle = subtitle)
    }

    private fun parseSound(argument: String): ActionInstruction.Sound? {
        val args = if ('|' in argument) argument.split("|") else argument.split(" ")
        val cleanArgs = args.filter { it.isNotBlank() }
        if (cleanArgs.isEmpty()) return null

        val sound = cleanArgs[0].trim().lowercase()
        val volume = cleanArgs.getOrNull(1)?.toFloatOrNull() ?: 1f
        val pitch = cleanArgs.getOrNull(2)?.toFloatOrNull() ?: 1f
        return ActionInstruction.Sound(sound, volume, pitch)
    }
}
package com.github.irvinglink.amethystLibKotlin.models.action

object ActionInstructionParser {

    fun parse(line: String): ActionInstruction? {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return null

        val split = trimmed.split(" ", limit = 2)
        val type = split[0].lowercase()
        val argument = split.getOrNull(1).orEmpty().trim()

        return when (type) {
            "[message]" -> {
                ActionInstruction.Message(argument)
            }

            "[console]" -> {
                ActionInstruction.Console(argument)
            }

            "[title]" -> {
                parseTitle(argument)
            }

            "[player]" -> {
                ActionInstruction.PlayerCommand(argument)
            }

            "[sound]" -> {
                parseSound(argument)
            }

            "[open_menu]" -> {
                ActionInstruction.OpenMenu(argument)
            }

            "[close_menu]" -> {
                ActionInstruction.CloseMenu
            }

            "[action_bar]" -> {
                ActionInstruction.ActionBar(argument)
            }

            else -> null
        }
    }

    fun parseAll(lines: List<String>): List<ActionInstruction> {
        return lines.mapNotNull(::parse)
    }

    private fun parseTitle(argument: String): ActionInstruction.Title {
        val titleParts = argument.split("subtitle:", limit = 2)
        val title = titleParts.getOrNull(0).orEmpty().trim()
        val subtitle = titleParts.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

        return ActionInstruction.Title(
            title = title,
            subtitle = subtitle
        )
    }

    private fun parseSound(argument: String): ActionInstruction.Sound? {
        val args = argument.split(" ").filter { it.isNotBlank() }

        if (args.isEmpty()) return null

        val sound = args[0]
        val volume = args.getOrNull(1)?.toFloatOrNull() ?: 1f
        val pitch = args.getOrNull(2)?.toFloatOrNull() ?: 1f

        return ActionInstruction.Sound(
            sound = sound,
            volume = volume,
            pitch = pitch
        )
    }
}
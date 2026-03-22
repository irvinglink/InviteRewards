package com.github.irvinglink.amethystLibKotlin.models.action

sealed class ActionInstruction {

    data class Message(val text: String) : ActionInstruction()

    data class Console(val command: String) : ActionInstruction()

    data class Title(
        val title: String,
        val subtitle: String? = null,
        val fadeIn: Int = 20,
        val stay: Int = 20,
        val fadeOut: Int = 20
    ) : ActionInstruction()

    data class PlayerCommand(val command: String) : ActionInstruction()

    data class Sound(
        val sound: String,
        val volume: Float = 1f,
        val pitch: Float = 1f
    ) : ActionInstruction()

    data class OpenMenu(val menuId: String) : ActionInstruction()

    data object CloseMenu : ActionInstruction()

    data class ActionBar(val text: String) : ActionInstruction()

}
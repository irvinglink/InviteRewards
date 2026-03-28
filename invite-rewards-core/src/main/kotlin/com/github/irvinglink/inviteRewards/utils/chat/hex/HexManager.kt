package com.github.irvinglink.inviteRewards.utils.chat.hex

import kotlin.math.roundToInt

/**
 * Handles Hex color codes and gradients for Chat.
 * Usage: HexManager.toChatColorString("your text")
 */
object HexManager {

    // Regex Patterns compiled for performance
    private val HEX_PATTERN = Regex("#[a-fA-F0-9]{6}")
    private val GRADIENT_PATTERN = Regex("<\\$#[0-9a-fA-F]{6}>[^<]*<\\$#[0-9a-fA-F]{6}>")
    private val FIX_BRACE = Regex("\\{#[0-9a-fA-F]{6}\\}")        // {#RRGGBB}
    private val FIX_ANGLE = Regex("<#[0-9a-fA-F]{6}>")            // <#RRGGBB>
    private val FIX_AMPERSAND = Regex("&x[&0-9a-fA-F]{12}")       // &x&R&R&G&G&B&B

    /**
     * Main entry point to colorize a string.
     */
    fun toChatColorString(textInput: String): String {
        val formatted = applyFormats(textInput)

        // Find all #RRGGBB and replace them with Minecraft color codes (§x§r...)
        return HEX_PATTERN.replace(formatted) { match ->
            toChatColor(match.value)
        }
    }

    private fun applyFormats(input: String): String {
        // 1. Fix &#RRGGBB -> #RRGGBB
        var text = input.replace("&#", "#")

        // 2. Fix {#RRGGBB} -> #RRGGBB
        text = FIX_BRACE.replace(text) { match ->
            "#" + match.value.substring(2, 8)
        }

        // 3. Fix <#RRGGBB> -> #RRGGBB
        text = FIX_ANGLE.replace(text) { match ->
            "#" + match.value.substring(2, 8)
        }

        // 4. Fix Legacy Bungee &x&r... -> #RRGGBB
        // We normalize section signs to ampersands first for this specific check
        text = text.replace('§', '&')
        text = FIX_AMPERSAND.replace(text) { match ->
            val s = match.value
            // Extract chars at odd positions: &x&R&R&G&G&B&B -> indices 3,5,7,9,11,13
            "#${s[3]}${s[5]}${s[7]}${s[9]}${s[11]}${s[13]}"
        }

        // 5. Handle Gradients <$#RRGGBB>Text<$#RRGGBB>
        text = GRADIENT_PATTERN.replace(text) { match ->
            val content = match.value
            val startHex = content.substring(3, 9)
            val message = content.substring(10, content.length - 10)
            val endHex = content.substring(content.length - 7, content.length - 1)

            asGradient(TextColor(startHex), message, TextColor(endHex))
        }

        return text
    }

    private fun toChatColor(hexCode: String): String {
        // Input: #RRGGBB -> Output: §x§R§R§G§G§B§B
        val builder = StringBuilder("§x")
        for (i in 1 until hexCode.length) {
            builder.append('§').append(hexCode[i])
        }
        return builder.toString()
    }

    private fun asGradient(start: TextColor, text: String, end: TextColor): String {
        val sb = StringBuilder()

        // Handle bold specifically inside gradients
        val cleanText = if (text.contains("&l")) text.replace("&l", "") else text
        val isBold = text.contains("&l")

        val length = cleanText.length
        if (length == 0) return ""

        for (i in 0 until length) {
            val ratio = i.toFloat() / (length - 1).coerceAtLeast(1)

            val red = (start.red + (end.red - start.red) * ratio).roundToInt()
            val green = (start.green + (end.green - start.green) * ratio).roundToInt()
            val blue = (start.blue + (end.blue - start.blue) * ratio).roundToInt()

            sb.append("#")
                .append(String.format("%02x%02x%02x", red, green, blue))
                .append(if (isBold) "&l" else "")
                .append(cleanText[i])
        }

        return sb.toString()
    }

    // Helper data class to handle colors easily
    private data class TextColor(val red: Int, val green: Int, val blue: Int) {
        constructor(hex: String) : this(
            Integer.valueOf(hex.substring(0, 2), 16),
            Integer.valueOf(hex.substring(2, 4), 16),
            Integer.valueOf(hex.substring(4, 6), 16)
        )
    }
}
package com.github.irvinglink.amethystLibKotlin.utils.chat.hex

class TextColor(hexCode: String) {
    val red: Int
    val green: Int
    val blue: Int

    init {
        val hexColor = hexCode.toInt(16)
        this.red = hexColor shr 16 and 0xFF
        this.green = hexColor shr 8 and 0xFF
        this.blue = hexColor and 0xFF
    }
}

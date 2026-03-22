package com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.providers

import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.PlaceholderProvider

class ValuePlaceholderProvider : PlaceholderProvider {

    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {
        registry["file_name"] = {it.value}
        registry["command_syntax"] = {it.value}
    }

}
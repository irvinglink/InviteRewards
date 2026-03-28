package com.github.irvinglink.inviteRewards.utils.chat.placeholder.providers

import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderContext
import com.github.irvinglink.inviteRewards.utils.chat.placeholder.PlaceholderProvider

class ValuePlaceholderProvider : PlaceholderProvider {

    override fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>) {
        registry["file_name"] = {it.value}
        registry["command_syntax"] = {it.value}
        registry["value"] = {it.value}
    }

}
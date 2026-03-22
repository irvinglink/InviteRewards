package com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder

import com.github.irvinglink.amethystLibKotlin.enums.config.MESSAGES
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.providers.PlayerPlaceholderProvider
import com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder.providers.ValuePlaceholderProvider

class PlaceholderRegistry {

    private val placeholders = mutableMapOf<String, (PlaceholderContext) -> String?>()

    init {
        register("prefix") { MESSAGES.PREFIX.message }

        registerProviders(
            PlayerPlaceholderProvider(),
            ValuePlaceholderProvider()
        )
    }

    fun register(key: String, resolver: (PlaceholderContext) -> String?) {
        placeholders[key.lowercase()] = resolver
    }

    fun registerProvider(provider: PlaceholderProvider) {
        provider.register(placeholders)
    }

    fun registerProviders(vararg providers: PlaceholderProvider) {
        providers.forEach { registerProvider(it) }
    }

    fun resolve(key: String, context: PlaceholderContext): String? {
        return placeholders[key.lowercase()]?.invoke(context)
    }

}
package com.github.irvinglink.amethystLibKotlin.utils.chat.placeholder

interface PlaceholderProvider {
    fun register(registry: MutableMap<String, (PlaceholderContext) -> String?>)
}
package com.github.irvinglink.amethystLibKotlin.features.guis.loader

import com.github.irvinglink.amethystLibKotlin.features.guis.menus.ExampleProgrammaticMenu

internal object DefaultMenusLoader {
    fun register() {
        MenuLoader.register("example_menu") { player -> ExampleProgrammaticMenu(player) }
    }
}
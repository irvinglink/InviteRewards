package com.github.irvinglink.amethystLibKotlin.features.guis.menus

import com.github.irvinglink.amethystLibKotlin.features.guis.manager.Menu
import com.github.irvinglink.amethystLibKotlin.features.guis.models.ItemMenu
import com.github.irvinglink.amethystLibKotlin.features.guis.models.MenuClickType
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.RequirementList
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.ExpRequirement
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.MoneyRequirement
import com.github.irvinglink.amethystLibKotlin.features.guis.models.requirements.types.PermissionRequirement
import com.github.irvinglink.amethystLibKotlin.models.action.ExecutableAction
import com.github.irvinglink.amethystLibKotlin.utils.items.CustomItems
import com.github.irvinglink.amethystLibKotlin.utils.items.defaults.DefaultItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class ExampleProgrammaticMenu(player: Player) : Menu(
    id          = "example_programmatic",
    title       = "&8&lExample &5&lMenu",
    rows        = 4,
    enableSlots = false
) {

    init {

        fillBorder(
            CustomItems.builder(Material.GRAY_STAINED_GLASS_PANE)
                .name("&r")
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build()
        )

        addItems(
            ItemMenu(
                id        = "info",
                itemStack = CustomItems.builder(Material.BOOK)
                    .name("&e&lWelcome, &f${player.name}&e&l!")
                    .lore(
                        "&7Browse the shop below.",
                        "&7Click items to purchase them.",
                        "",
                        "&eYour level: &f${player.level}"
                    )
                    .glow()
                    .build(),
                slot = 10
            )
        )

        addItems(
            ItemMenu(
                id        = "diamond_buy",
                itemStack = CustomItems.builder(Material.DIAMOND)
                    .name("&b&lBuy Diamond")
                    .lore(
                        "&7Left click &8» &fBuy &b1 &7for &a\$100",
                        "&7Shift-Left &8» &fBuy &b64 &7for &a\$6,400",
                        "&7Right click &8» &fView prices",
                        "",
                        "&eRequires: &a\$100"
                    )
                    .build(),
                slot = 12,
                requirements = RequirementList(listOf(
                    MoneyRequirement(
                        amount      = 100,
                        withdraw    = true,
                        failActions = listOf(
                            "[message] &cYou need at least &4\$100&c!",
                            "[sound] ENTITY_VILLAGER_NO 1.0 1.0",
                            "[action_bar] &c✗ Not enough money"
                        )
                    )
                )),
                actions = mapOf(
                    MenuClickType.LEFT to ExecutableAction.fromInstructions(listOf(
                        "[console] give ${player.name} diamond 1",
                        "[message] &aYou bought &b1 diamond &afor &2\$100&a!",
                        "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.2",
                        "[action_bar] &a+1 Diamond"
                    )),
                    MenuClickType.SHIFT_LEFT to ExecutableAction.fromInstructions(listOf(
                        "[console] give ${player.name} diamond 64",
                        "[message] &aYou bought &b64 diamonds &afor &2\$6,400&a!",
                        "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.5",
                        "[action_bar] &a+64 Diamonds"
                    )),
                    MenuClickType.RIGHT to ExecutableAction.fromInstructions(listOf(
                        "[message] &e&lDiamond Shop Prices:",
                        "[message] &8» &b1 diamond &8| &a\$100",
                        "[message] &8» &b64 diamonds &8| &a\$6,400"
                    ))
                )
            )
        )

        addItems(
            ItemMenu(
                id        = "emerald_buy",
                itemStack = CustomItems.builder(Material.EMERALD)
                    .name("&a&lBuy Emerald")
                    .lore(
                        "&7Left click &8» &fBuy &a1 &7for &b5 levels",
                        "&7Shift-Left &8» &fBuy &a16 &7for &b64 levels",
                        "",
                        "&eRequires: &b5 levels"
                    )
                    .build(),
                slot = 14,
                requirements = RequirementList(listOf(
                    ExpRequirement(
                        amount      = 5,
                        withdraw    = true,
                        failActions = listOf(
                            "[message] &cYou need at least &45 levels&c!",
                            "[sound] ENTITY_VILLAGER_NO 1.0 1.0",
                            "[action_bar] &c✗ Not enough levels"
                        )
                    )
                )),
                actions = mapOf(
                    MenuClickType.LEFT to ExecutableAction.fromInstructions(listOf(
                        "[console] give ${player.name} emerald 1",
                        "[message] &aYou bought &a1 emerald &afor &b5 levels&a!",
                        "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.2",
                        "[action_bar] &a+1 Emerald"
                    )),
                    MenuClickType.SHIFT_LEFT to ExecutableAction.fromInstructions(listOf(
                        "[console] give ${player.name} emerald 16",
                        "[message] &aYou bought &a16 emeralds &afor &b64 levels&a!",
                        "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.5",
                        "[action_bar] &a+16 Emeralds"
                    ))
                )
            )
        )

        addItems(
            ItemMenu(
                id        = "gold_buy",
                itemStack = CustomItems.builder(Material.GOLD_INGOT)
                    .name("&6&lBuy Gold Ingot")
                    .lore(
                        "&7Left click &8» &fBuy &61 &7for &a\$50",
                        "",
                        "&eRequires: &7example.shop.gold",
                        "&eRequires: &a\$50"
                    )
                    .build(),
                slot = 16,
                requirements = RequirementList(listOf(
                    PermissionRequirement(
                        permission  = "example.shop.gold",
                        failActions = listOf(
                            "[message] &cYou don't have permission to buy gold!",
                            "[sound] ENTITY_VILLAGER_NO 1.0 1.0"
                        )
                    ),
                    MoneyRequirement(
                        amount      = 50,
                        withdraw    = true,
                        failActions = listOf(
                            "[message] &cYou need at least &4\$50&c!",
                            "[sound] ENTITY_VILLAGER_NO 1.0 1.0",
                            "[action_bar] &c✗ Not enough money"
                        )
                    )
                )),
                actions = mapOf(
                    MenuClickType.DEFAULT to ExecutableAction.fromInstructions(listOf(
                        "[console] give ${player.name} gold_ingot 1",
                        "[message] &aYou bought &61 gold ingot &afor &2\$50&a!",
                        "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.2",
                        "[action_bar] &a+1 Gold Ingot"
                    ))
                )
            )
        )

        addItems(
            ItemMenu(
                id        = "title_demo",
                itemStack = CustomItems.builder(Material.FIREWORK_ROCKET)
                    .name("&d&lTitle Demo")
                    .lore("&7Click to see a title!")
                    .build(),
                slot = 22,
                actions = mapOf(
                    MenuClickType.DEFAULT to ExecutableAction.fromInstructions(listOf(
                        "[title] &d&lHello, ${player.name}! subtitle: &7Welcome to the shop",
                        "[sound] UI_TOAST_CHALLENGE_COMPLETE 0.5 1.0"
                    ))
                )
            )
        )

        addItems(
            ItemMenu(
                id        = "close",
                itemStack = CustomItems.get(DefaultItems.CLOSE_MENU),
                slot      = 31,
                actions   = mapOf(
                    MenuClickType.DEFAULT to ExecutableAction.fromInstructions(listOf(
                        "[close_menu]",
                        "[message] &7See you next time, &f${player.name}&7!"
                    ))
                )
            )
        )
    }
}
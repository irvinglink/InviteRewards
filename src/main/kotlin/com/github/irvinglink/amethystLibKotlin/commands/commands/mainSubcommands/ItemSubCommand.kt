package com.github.irvinglink.amethystLibKotlin.commands.commands.mainSubcommands

import com.cryptomorin.xseries.XMaterial
import com.github.irvinglink.amethystLibKotlin.commands.builders.SubCommand
import com.github.irvinglink.amethystLibKotlin.enums.config.ITEMTAGS
import com.github.irvinglink.amethystLibKotlin.utils.data.items.ItemDataStorage
import com.github.irvinglink.amethystLibKotlin.utils.items.CustomItems
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemSubCommand : SubCommand {

    override val name: String
        get() = "item"

    override val description: String
        get() = "Item example for NBT / Compound test"

    override val syntax: String
        get() = "/amethyst item"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) return

        val player = sender

        val exampleItem: ItemStack = CustomItems.builder(XMaterial.STICK.parseMaterial()!!, 1)
            .data(ITEMTAGS.ITEM_OWNER.keyName, player.name)
            .glow()
            .build()

        player.inventory.addItem(exampleItem)

        val value: String? = ItemDataStorage.of(exampleItem)
            .getString(ITEMTAGS.ITEM_OWNER.keyName)

        player.sendMessage(
            chat.format("%amethystlib_prefix% &bData: &f${value ?: "null"}")
        )
    }
}
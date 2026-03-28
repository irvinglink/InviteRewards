package com.github.irvinglink.inviteRewards.utils.base64

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

object BukkitSerializer {

    fun itemStacksToBase64(contents: Array<ItemStack?>): String {
        return try {
            ByteArrayOutputStream().use { outputStream ->
                BukkitObjectOutputStream(outputStream).use { bukkitOutputStream ->
                    bukkitOutputStream.writeObject(contents)
                }

                Base64Coder.encodeLines(outputStream.toByteArray())
            }
        } catch (exception: IOException) {
            throw IllegalStateException("Failed to serialize ItemStack array to Base64", exception)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun itemStacksFromBase64(base64: String): Array<ItemStack?> {
        return try {
            ByteArrayInputStream(Base64Coder.decodeLines(base64)).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { bukkitInputStream ->
                    bukkitInputStream.readObject() as Array<ItemStack?>
                }
            }
        } catch (exception: IOException) {
            throw IllegalStateException("Failed to deserialize Base64 into ItemStack array", exception)
        } catch (exception: ClassNotFoundException) {
            throw IllegalStateException("Could not find class while deserializing ItemStack array", exception)
        }
    }

    fun itemStackToBase64(itemStack: ItemStack?): String {
        return itemStacksToBase64(arrayOf(itemStack))
    }

    fun itemStackFromBase64(base64: String): ItemStack? {
        return itemStacksFromBase64(base64).firstOrNull()
    }

}
package com.github.irvinglink.amethystLibKotlin.utils.other

import java.util.*

class TurnUtils<T> {
    private val itemQueue: Queue<T?> = LinkedList<T?>()
    private var currentItem: T? = null

    // Add an item to the turn queue
    fun addItem(item: T?) {
        if (itemQueue.contains(item)) return
        itemQueue.offer(item)
    }

    // Remove an item from the turn queue
    fun removeItem(item: T?) {
        if (itemQueue.contains(item)) {
            itemQueue.remove(item)
            // If the removed item was the current turn, advance to the next turn
            if (item == currentItem) {
                advanceTurn()
            }
        }
    }

    // Advance to the next turn
    fun advanceTurn() {
        if (!itemQueue.isEmpty()) {
            currentItem = itemQueue.poll()
            itemQueue.offer(currentItem)
        } else {
            currentItem = null
        }
    }

    val currentTurn: T?
        // Get the item whose turn it is
        get() {
            if (currentItem == null) advanceTurn()
            return currentItem
        }

    // Check if it's the given item's turn
    fun isItemTurn(item: T?): Boolean {
        if (currentItem == null) advanceTurn()
        return item == currentItem
    }

    // Perform an action if it's the item's turn
    fun itemAction(item: T?): Boolean {
        if (isItemTurn(item)) {
            advanceTurn()
            return true
        } else {
            return false
        }
    }

    // Returns the queue size
    fun turnsSize(): Int {
        return itemQueue.size
    }
}
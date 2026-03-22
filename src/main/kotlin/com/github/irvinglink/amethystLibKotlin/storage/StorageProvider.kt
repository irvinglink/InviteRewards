package com.github.irvinglink.amethystLibKotlin.storage

interface StorageProvider {

    fun setup()

    fun shutdown()

    fun isReady(): Boolean

}
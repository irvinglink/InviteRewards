package com.github.irvinglink.inviteRewards.storage

interface StorageProvider {

    fun setup()

    fun shutdown()

    fun isReady(): Boolean

}
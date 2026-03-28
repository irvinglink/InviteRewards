package com.github.irvinglink.inviteRewards.storage

data class StorageConfig(
    val type: StorageType,
    val sql: SQLConfig = SQLConfig(),
    val yaml: YamlConfig = YamlConfig()
)

data class SQLConfig(
    val host: String = "localhost",
    val port: Int = 3306,
    val database: String = "database",
    val username: String = "root",
    val password: String = ""
)

data class YamlConfig(
    val folder: String = "data"
)
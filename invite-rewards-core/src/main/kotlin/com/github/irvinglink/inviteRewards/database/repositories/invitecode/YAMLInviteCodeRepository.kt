package com.github.irvinglink.inviteRewards.database.repositories.invitecode

import com.github.irvinglink.inviteRewards.database.models.InviteCodeData
import com.github.irvinglink.inviteRewards.storage.providers.yaml.YamlStorage
import java.util.UUID
import java.util.concurrent.CompletableFuture

class YamlInviteCodeRepository(
    private val storage: YamlStorage
) : InviteCodeRepository {

    private val file = storage.getFile("invite_codes")

    private fun <T> resolved(value: T): CompletableFuture<T> =
        CompletableFuture.completedFuture(value)

    override fun save(data: InviteCodeData): CompletableFuture<Unit> {
        val path = "codes.${data.inviteTypeId}.${data.code}"
        file.set("$path.owner_uuid", data.ownerUuid.toString())
        file.set("$path.active", data.active)
        file.set("$path.manual", data.manual)
        file.set("$path.created_at", data.createdAt)
        file.set("$path.created_by", data.createdBy?.toString())
        file.save()
        return resolved(Unit)
    }

    override fun load(code: String, inviteTypeId: String): CompletableFuture<InviteCodeData?> {
        val path = "codes.$inviteTypeId.$code"
        if (!file.contains(path)) return resolved(null)

        val ownerUuid = file.getString("$path.owner_uuid") ?: return resolved(null)

        return resolved(
            InviteCodeData(
                code = code,
                ownerUuid = UUID.fromString(ownerUuid),
                inviteTypeId = inviteTypeId,
                active = file.getBoolean("$path.active", true),
                manual = file.getBoolean("$path.manual", false),
                createdAt = file.getLong("$path.created_at"),
                createdBy = file.getString("$path.created_by")?.let(UUID::fromString)
            )
        )
    }

    override fun loadByOwner(ownerUuid: UUID): CompletableFuture<List<InviteCodeData>> {
        val root = file.getConfigurationSection("codes") ?: return resolved(emptyList())

        val list = mutableListOf<InviteCodeData>()

        for (inviteTypeId in root.getKeys(false)) {
            val typeSection = root.getConfigurationSection(inviteTypeId) ?: continue

            for (code in typeSection.getKeys(false)) {
                val path = "codes.$inviteTypeId.$code"
                val owner = file.getString("$path.owner_uuid") ?: continue

                if (owner != ownerUuid.toString()) continue

                list.add(
                    InviteCodeData(
                        code = code,
                        ownerUuid = UUID.fromString(owner),
                        inviteTypeId = inviteTypeId,
                        active = file.getBoolean("$path.active", true),
                        manual = file.getBoolean("$path.manual", false),
                        createdAt = file.getLong("$path.created_at"),
                        createdBy = file.getString("$path.created_by")?.let(UUID::fromString)
                    )
                )
            }
        }

        return resolved(list.sortedBy { it.createdAt })
    }

    override fun loadByOwnerAndType(ownerUuid: UUID, inviteTypeId: String): CompletableFuture<List<InviteCodeData>> {
        val typeSection = file.getConfigurationSection("codes.$inviteTypeId")
            ?: return resolved(emptyList())

        val list = typeSection.getKeys(false).mapNotNull { code ->
            val path = "codes.$inviteTypeId.$code"
            val owner = file.getString("$path.owner_uuid") ?: return@mapNotNull null

            if (owner != ownerUuid.toString()) return@mapNotNull null

            InviteCodeData(
                code = code,
                ownerUuid = UUID.fromString(owner),
                inviteTypeId = inviteTypeId,
                active = file.getBoolean("$path.active", true),
                manual = file.getBoolean("$path.manual", false),
                createdAt = file.getLong("$path.created_at"),
                createdBy = file.getString("$path.created_by")?.let(UUID::fromString)
            )
        }.sortedBy { it.createdAt }

        return resolved(list)
    }

    override fun delete(code: String, inviteTypeId: String): CompletableFuture<Unit> {
        file.set("codes.$inviteTypeId.$code", null)
        file.save()
        return resolved(Unit)
    }

    override fun exists(code: String, inviteTypeId: String): CompletableFuture<Boolean> =
        resolved(file.contains("codes.$inviteTypeId.$code"))
}
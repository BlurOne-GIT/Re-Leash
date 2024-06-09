package code.blurone.releash

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ReLeash : JavaPlugin(), Listener {
    companion object {
        private val defaultEntities = listOf(
            EntityType.ALLAY,          /*EntityType.ARMADILLO,*/  EntityType.AXOLOTL,      EntityType.BEE,
            /*EntityType.BOAT,*/       EntityType.CAMEL,          EntityType.CAT,          EntityType.CHICKEN,
            EntityType.COW,            EntityType.DOLPHIN,        EntityType.DONKEY,       EntityType.FOX,
            EntityType.FROG,           EntityType.GLOW_SQUID,     EntityType.GOAT,         EntityType.HOGLIN,
            EntityType.HORSE,          EntityType.IRON_GOLEM,     EntityType.LLAMA,        EntityType.MUSHROOM_COW,
            EntityType.MULE,           EntityType.OCELOT,         EntityType.PARROT,       EntityType.PIG,
            EntityType.POLAR_BEAR,     EntityType.RABBIT,         EntityType.SHEEP,        EntityType.SKELETON_HORSE,
            EntityType.SNIFFER,        EntityType.SNOWMAN,        EntityType.SQUID,        EntityType.STRIDER,
            EntityType.TRADER_LLAMA,   EntityType.WOLF,           EntityType.ZOGLIN,       EntityType.ZOMBIE_HORSE
        ).sorted()

        fun binaryHasEntity(list: List<EntityType>, type: EntityType) = list.binarySearch(type) >= 0
        fun isDefaultEntity(type: EntityType): Boolean = binaryHasEntity(defaultEntities, type)
    }

    private lateinit var theList: List<EntityType>
    private val isWhitelist = config.getStringList("whitelist").isNotEmpty()
    private val playerLeashing = config.getBoolean("player-leashing", false)

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        if (playerLeashing)
            server.pluginManager.registerEvents(PlayerUnleasher(this), this)

        val blacklist = config.getStringList("blacklist")
            .mapNotNull { try { EntityType.valueOf(it) } catch (_: Exception) { null } }

        if (!isWhitelist) {
            val (defaultBlacklist, autofillBlacklist) = blacklist.sorted().partition(::isDefaultEntity)
            theList = autofillBlacklist
            if (defaultBlacklist.isNotEmpty())
                server.pluginManager.registerEvents(BlacklistedUnleasher(defaultBlacklist), this)
            return
        }

        theList = config.getStringList("whitelist")
            .mapNotNull { try { EntityType.valueOf(it) } catch (_: Exception) { null } }
            .filter { !isDefaultEntity(it) }
            .sorted()

        if (blacklist.isNotEmpty())
            server.pluginManager.registerEvents(BlacklistedUnleasher(
                blacklist.filter(::isDefaultEntity).sorted()
            ), this)
    }

    @EventHandler
    private fun onPlayerInteractEvent(event: PlayerInteractEntityEvent) {
        if (event.player.gameMode == GameMode.SPECTATOR) return

        val itemStack = event.player.inventory.getItem(event.hand) ?: return
        if (itemStack.type != Material.LEAD) return

        val livingEntity = event.rightClicked as? LivingEntity ?: return
        if (
            livingEntity.isLeashed ||
            isDefaultEntity(livingEntity.type) ||
            (livingEntity.type == EntityType.PLAYER && !playerLeashing) ||
            (livingEntity.type != EntityType.PLAYER && binaryHasEntity(theList, livingEntity.type) != isWhitelist)
        ) return

        if (!livingEntity.setLeashHolder(event.player)) return

        if (event.hand == EquipmentSlot.HAND)
            event.player.swingMainHand()
        else
            event.player.swingOffHand()

        if (event.player.gameMode != GameMode.CREATIVE)
            --itemStack.amount
    }
}

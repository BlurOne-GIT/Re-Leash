package code.blurone.releash

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

@Suppress("unused")
class ReLeash : JavaPlugin(), Listener {
    private val defaultEntities = setOf(
        EntityType.ALLAY,          /*EntityType.ARMADILLO,*/  EntityType.AXOLOTL,      EntityType.BEE,
        /*EntityType.BOAT,*/       EntityType.CAMEL,          EntityType.CAT,          EntityType.CHICKEN,
        EntityType.COW,            EntityType.DOLPHIN,        EntityType.DONKEY,       EntityType.FOX,
        EntityType.FROG,           EntityType.GLOW_SQUID,     EntityType.GOAT,         EntityType.HOGLIN,
        EntityType.HORSE,          EntityType.IRON_GOLEM,     EntityType.LLAMA,        EntityType.MUSHROOM_COW,
        EntityType.MULE,           EntityType.OCELOT,         EntityType.PARROT,       EntityType.PIG,
        EntityType.POLAR_BEAR,     EntityType.RABBIT,         EntityType.SHEEP,        EntityType.SKELETON_HORSE,
        EntityType.SNIFFER,        EntityType.SNOWMAN,        EntityType.SQUID,        EntityType.STRIDER,
        EntityType.TRADER_LLAMA,   EntityType.WOLF,           EntityType.ZOGLIN,       EntityType.ZOMBIE_HORSE
    )

    private lateinit var theList: Set<EntityType>
    private val isWhitelist = config.getStringList("whitelist").isNotEmpty()

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        val blacklist = config.getStringList("blacklist")
            .mapNotNull { try { EntityType.valueOf(it) } catch (_: Exception) { null } }
            .toSet()

        if (!isWhitelist) {
            val (defaultBlacklist, autofillBlacklist) = blacklist.partition(defaultEntities::contains)
            theList = autofillBlacklist.toSet()
            if (defaultBlacklist.isNotEmpty())
                server.pluginManager.registerEvents(BlacklistedUnleasher(defaultBlacklist.toSet()), this)
            return
        }

        theList = config.getStringList("whitelist")
            .mapNotNull { try { EntityType.valueOf(it) } catch (_: Exception) { null } }
            .filter { !defaultEntities.contains(it) }
            .toSet()

        if (blacklist.isNotEmpty())
            server.pluginManager.registerEvents(BlacklistedUnleasher(
                blacklist.filter(defaultEntities::contains).toSet()
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
            defaultEntities.contains(livingEntity.type) ||
            livingEntity.type == EntityType.PLAYER ||
            theList.contains(livingEntity.type) != isWhitelist
        ) return

        object : BukkitRunnable() {
            override fun run() {
                livingEntity.setLeashHolder(event.player)
            }
        }.runTaskLater(this, 0L)

        if (event.hand == EquipmentSlot.HAND)
            event.player.swingMainHand()
        else
            event.player.swingOffHand()

        if (event.player.gameMode != GameMode.CREATIVE)
            --itemStack.amount
    }
}

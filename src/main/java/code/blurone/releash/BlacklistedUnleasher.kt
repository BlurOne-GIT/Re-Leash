package code.blurone.releash

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerLeashEntityEvent

class BlacklistedUnleasher(private val blacklist: Set<EntityType>) : Listener {
    @EventHandler
    private fun onPlayerLeash(event: PlayerLeashEntityEvent) {
        event.isCancelled = blacklist.contains(event.entity.type)
    }
}
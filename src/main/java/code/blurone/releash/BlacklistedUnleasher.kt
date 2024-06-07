package code.blurone.releash

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerLeashEntityEvent

class BlacklistedUnleasher(private val blacklist: List<EntityType>) : Listener {
    @EventHandler
    private fun onPlayerLeash(event: PlayerLeashEntityEvent) {
        event.isCancelled = ReLeash.binaryHasEntity(blacklist, event.entity.type)
    }
}
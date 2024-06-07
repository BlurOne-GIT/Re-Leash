package code.blurone.releash

import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class PlayerUnleasher(plugin: Plugin) : Listener {
    private val crouchTimerNamespacedKey = NamespacedKey(plugin, "crouch_timer")

    @EventHandler
    private fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        if (!event.player.isLeashed) return

        val crouchTime = event.player.persistentDataContainer.get(crouchTimerNamespacedKey, PersistentDataType.LONG) ?: 0
        if (event.player.world.gameTime <= crouchTime + 17) {
            event.player.persistentDataContainer.remove(crouchTimerNamespacedKey)
            if (event.player.setLeashHolder(null)) return
        }

        event.player.persistentDataContainer.set(crouchTimerNamespacedKey, PersistentDataType.LONG, event.player.world.gameTime)
    }
}
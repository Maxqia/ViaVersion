package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaListener;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

public class DeathListener extends ViaListener {
    public DeathListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_9TO1_8.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (isOnPipe(p) && ViaVersion.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld()) && e.getDeathMessage() != null)
            sendPacket(p, e.getDeathMessage());
    }

    public boolean checkGamerule(World w) {
        try {
            return Boolean.parseBoolean(w.getGameRuleValue("showDeathMessages"));
        } catch (Exception e) {
            return false;
        }
    }

    private void sendPacket(final Player p, final String msg) {
        Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                PacketWrapper wrapper = new PacketWrapper(0x2C, null, getUserConnection(p));
                try {
                    wrapper.write(Type.VAR_INT, 2); // Event - Entity dead
                    wrapper.write(Type.VAR_INT, p.getEntityId()); // Player ID
                    wrapper.write(Type.INT, p.getEntityId()); // Entity ID
                    Protocol1_9TO1_8.FIX_JSON.write(wrapper, msg); // Message

                    wrapper.send(Protocol1_9TO1_8.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

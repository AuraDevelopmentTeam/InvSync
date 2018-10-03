package world.jnc.invsync.util.serializer.module;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.permission.PermissionRegistry;

public abstract class SyncModule {
  public static String getPermissionPrefix() {
    return PermissionRegistry.SYNC;
  }

  public abstract String getName();

  public String getPermission() {
    return getPermissionPrefix() + getName();
  }

  public String getSettingName() {
    return getName();
  }

  public boolean isEnabled() {
    return InventorySync.getConfig().getSynchronize(getName());
  }

  public boolean syncPlayer(Player player) {
    return isEnabled() && player.hasPermission(getPermission());
  }

  public abstract DataContainer serialize(Player player);

  public abstract void deserialize(Player player, DataContainer container);
}

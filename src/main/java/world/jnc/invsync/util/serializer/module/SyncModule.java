package world.jnc.invsync.util.serializer.module;

import lombok.AccessLevel;
import lombok.Getter;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.permission.PermissionRegistry;

public abstract class SyncModule {
  @Getter(value = AccessLevel.PROTECTED, lazy = true)
  private final DataQuery query = DataQuery.of(getName());

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

  public boolean isEnabled(Player player) {
    return true && player.hasPermission(getPermission());
  }

  public abstract DataContainer serialize(Player player);

  public abstract void deserialize(Player player, DataContainer container);
}

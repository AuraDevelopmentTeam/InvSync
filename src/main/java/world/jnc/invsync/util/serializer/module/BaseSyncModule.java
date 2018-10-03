package world.jnc.invsync.util.serializer.module;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.permission.PermissionRegistry;

public abstract class BaseSyncModule {
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

  public boolean isEnabled() {
    return InventorySync.getConfig().getSynchronize(getName());
  }

  public boolean syncPlayer(Player player) {
    return isEnabled() && player.hasPermission(getPermission());
  }

  public final DataView serialize(Player player) {
    return serialize(null, Optional.empty());
  }

  public final DataView serialize(Player player, Optional<DataView> container) {
    return serialize(player, container.orElseGet(DataContainer::createNew));
  }

  public abstract DataView serialize(Player player, DataView container);

  public abstract void deserialize(Player player, DataView container);
}

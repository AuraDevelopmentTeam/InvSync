package world.jnc.invsync.util.serializer.module;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.serializer.InventorySerializer;

public class EnderChestSyncModule extends BaseSyncModule {
  @Override
  public String getName() {
    return "ender_chest";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(THIS, InventorySerializer.serializeInventory(player.getEnderChestInventory()));

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<List<DataView>> enderChest = container.getViewList(THIS);

    if (enderChest.isPresent()) {
      boolean fail =
          InventorySerializer.deserializeInventory(
              enderChest.get(), player.getEnderChestInventory());

      if (fail) {
        InventorySync.getLogger()
            .error(
                "Could not load properly the enderchest inventory of player "
                    + DataSource.getPlayerString(player)
                    + " because there where unknown item.");
        InventorySync.getLogger()
            .warn(
                "Please make sure you are using the same mods on all servers you are synchronizing with.");
      }
    }

    if (getDebug()) {
      getLogger().info("\t\tisPresent:");
      getLogger().info("\t\t\tenderChest:\t" + enderChest.isPresent());
    }
  }
}

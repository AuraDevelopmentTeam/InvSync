package world.jnc.invsync.util.serializer.module;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
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
      InventorySerializer.deserializeInventory(enderChest.get(), player.getEnderChestInventory());
    }

    if (getDebug()) {
      getLogger().info("\t\tisPresent:");
      getLogger().info("\t\t\tenderChest:\t" + enderChest.isPresent());
    }
  }
}

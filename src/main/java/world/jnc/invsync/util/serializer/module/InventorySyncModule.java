package world.jnc.invsync.util.serializer.module;

import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.serializer.InventorySerializer;

public class InventorySyncModule extends BaseSyncModule {
  private static final DataQuery INVENTORY = DataQuery.of("inventory");
  private static final DataQuery SELECTED_SLOT = DataQuery.of("selectedSlot");

  private static Hotbar getHotbar(Player player) {
    return ((PlayerInventory) player.getInventory()).getHotbar();
  }

  @Override
  public String getName() {
    return "inventory";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(INVENTORY, InventorySerializer.serializeInventory(player.getInventory()));
    container.set(SELECTED_SLOT, getHotbar(player).getSelectedSlotIndex());

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<List<DataView>> inventory = container.getViewList(INVENTORY);
    Optional<Integer> selectedSlot = container.getInt(SELECTED_SLOT);

    if (inventory.isPresent()) {
      boolean fail =
          InventorySerializer.deserializeInventory(inventory.get(), player.getInventory());

      if (selectedSlot.isPresent()) {
        getHotbar(player).setSelectedSlotIndex(selectedSlot.get());
      }

      if (fail) {
        InventorySync.getLogger()
            .error(
                "Could not load inventory of player "
                    + DataSource.getPlayerString(player)
                    + " because there where unknown item.");
        InventorySync.getLogger()
            .warn(
                "Please make sure you are using the same mods on all servers you are synchronizing with.");
      }
    }

    if (getDebug()) {
      getLogger().info("\t\tisPresent:");
      getLogger().info("\t\t\tinventory:\t" + inventory.isPresent());
      getLogger().info("\t\t\tselectedSlot:\t" + selectedSlot.isPresent());
    }
  }
}

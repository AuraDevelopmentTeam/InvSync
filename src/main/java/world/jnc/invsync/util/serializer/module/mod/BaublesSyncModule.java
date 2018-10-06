package world.jnc.invsync.util.serializer.module.mod;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import world.jnc.invsync.util.serializer.InventorySerializer;

public class BaublesSyncModule extends BaseModSyncModule {
  @Getter(value = AccessLevel.PRIVATE, lazy = true)
  private static final QueryOperation<Class<? extends Inventory>> baublesInventoryQuery =
      QueryOperationTypes.INVENTORY_TYPE.of(getBaublesContainerClass());

  @SuppressWarnings("unchecked")
  private static Class<? extends Inventory> getBaublesContainerClass() {
    try {
      return (Class<? extends Inventory>) Class.forName("baubles.api.cap.BaublesContainer");
    } catch (ClassNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @Override
  public String getModId() {
    return "baubles";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(
        THIS,
        InventorySerializer.serializeInventory(
            player.getInventory().query(getBaublesInventoryQuery())));

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<List<DataView>> baublesSlots = container.getViewList(THIS);

    if (baublesSlots.isPresent()) {
      InventorySerializer.deserializeInventory(
          baublesSlots.get(), player.getInventory().query(getBaublesInventoryQuery()));
    }

    // TODO: Debug Logging
  }
}

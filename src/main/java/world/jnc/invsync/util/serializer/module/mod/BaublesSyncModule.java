package world.jnc.invsync.util.serializer.module.mod;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class BaublesSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "baubles";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    return Helper.serialize(player, container);
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Helper.deserialize(player, container);
  }

  @UtilityClass
  private static class Helper {
    private static DataView serialize(Player player, DataView container) {
      IBaublesItemHandler inventory =
          BaublesApi.getBaublesHandler(NativeInventorySerializer.getNativePlayer(player));
      container.set(THIS, NativeInventorySerializer.serializeInventory(inventory));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      IBaublesItemHandler inventory =
          BaublesApi.getBaublesHandler(NativeInventorySerializer.getNativePlayer(player));
      Optional<List<DataView>> baublesSlots = container.getViewList(THIS);

      if (baublesSlots.isPresent()) {
        NativeInventorySerializer.deserializeInventory(baublesSlots.get(), inventory);
      }

      // TODO: Debug Logging
    }
  }
}

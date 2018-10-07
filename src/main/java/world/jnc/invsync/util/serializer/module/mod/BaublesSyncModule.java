package world.jnc.invsync.util.serializer.module.mod;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayer;
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
    IBaublesItemHandler inventory = BaublesApi.getBaublesHandler((EntityPlayer) player);
    container.set(THIS, NativeInventorySerializer.serializeInventory(inventory));

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    IBaublesItemHandler inventory = BaublesApi.getBaublesHandler((EntityPlayer) player);
    Optional<List<DataView>> baublesSlots = container.getViewList(THIS);

    if (baublesSlots.isPresent()) {
      NativeInventorySerializer.deserializeInventory(baublesSlots.get(), inventory);
    }

    // TODO: Debug Logging
  }
}

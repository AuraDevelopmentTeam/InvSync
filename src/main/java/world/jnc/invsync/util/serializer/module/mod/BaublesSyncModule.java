package world.jnc.invsync.util.serializer.module.mod;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;

public class BaublesSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "baubles";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    // TODO Auto-generated method stub
    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    // TODO Auto-generated method stub

  }
}

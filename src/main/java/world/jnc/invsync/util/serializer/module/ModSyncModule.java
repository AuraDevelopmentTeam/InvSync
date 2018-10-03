package world.jnc.invsync.util.serializer.module;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public abstract class ModSyncModule extends SyncModule {
  public abstract String getModId();

  @Override
  public boolean isEnabled(Player player) {
    return Sponge.getPluginManager().isLoaded(getModId()) && super.isEnabled(player);
  }
}

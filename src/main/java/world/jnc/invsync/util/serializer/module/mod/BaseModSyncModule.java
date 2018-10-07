package world.jnc.invsync.util.serializer.module.mod;

import org.spongepowered.api.Sponge;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;

public abstract class BaseModSyncModule extends BaseSyncModule {
  public abstract String getModId();

  @Override
  public String getName() {
    return "mod." + getModId();
  }

  @Override
  public boolean isEnabled() {
    return Sponge.getPluginManager().isLoaded(getModId()) && super.isEnabled();
  }
}

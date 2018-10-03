package world.jnc.invsync.util.serializer.module;

import org.spongepowered.api.Sponge;

public abstract class ModSyncModule extends SyncModule {
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

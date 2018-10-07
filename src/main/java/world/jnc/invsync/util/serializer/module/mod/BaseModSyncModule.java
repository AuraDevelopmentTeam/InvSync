package world.jnc.invsync.util.serializer.module.mod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;

public abstract class BaseModSyncModule extends BaseSyncModule {
  @SuppressFBWarnings(
    value = "BC_UNCONFIRMED_CAST",
    justification =
        "Player must be a EntityPlayer.\n"
            + "If this is not the case, it's a good thing this blows up with an Error rather than an exception."
  )
  protected static EntityPlayer getNativePlayer(Player player) {
    return (EntityPlayer) player;
  }

  public abstract String getModId();

  @Override
  public String getName() {
    return "mod." + getModId();
  }
  
  public boolean canBeEnabled() {
    return Sponge.getPluginManager().isLoaded(getModId());
  }

  @Override
  public boolean isEnabled() {
    return canBeEnabled() && super.isEnabled();
  }
}

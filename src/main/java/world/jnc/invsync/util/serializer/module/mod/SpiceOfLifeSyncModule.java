package world.jnc.invsync.util.serializer.module.mod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import squeek.spiceoflife.foodtracker.FoodHistory;
import world.jnc.invsync.util.serializer.CapabilitySerializer;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class SpiceOfLifeSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "spiceoflife";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    return Helper.serialize(player, container);
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Helper.deserialize(player, container);
  }

  @SuppressFBWarnings(
    value = "NP_NULL_PARAM_DEREF_NONVIRTUAL",
    justification = "Capabilities aren't null during runtime (but compile time)."
  )
  @UtilityClass
  private static class Helper {
    private static DataView serialize(Player player, DataView container) {
      container.set(
          THIS,
          CapabilitySerializer.serializeCapabilityToData(
              FoodHistory.CAPABILITY, NativeInventorySerializer.getNativePlayer(player)));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<DataView> foodHistory = container.getView(THIS);

      if (foodHistory.isPresent()) {
        CapabilitySerializer.deserializeCapabilityFromData(
            FoodHistory.CAPABILITY,
            NativeInventorySerializer.getNativePlayer(player),
            foodHistory.get());
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tfoodHistory:\t" + foodHistory.isPresent());
      }
    }
  }
}

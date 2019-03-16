package world.jnc.invsync.util.serializer.module.mod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import toughasnails.api.TANCapabilities;
import world.jnc.invsync.util.serializer.CapabilitySerializer;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class ToughAsNailsSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "toughasnails";
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
    private static final DataQuery TEMPERATURE = DataQuery.of("temperature");
    private static final DataQuery THIRST = DataQuery.of("thirst");

    private static DataView serialize(Player player, DataView container) {
      final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);

      container.set(
          TEMPERATURE,
          CapabilitySerializer.serializeCapabilityToData(
              TANCapabilities.TEMPERATURE, nativePlayer));
      container.set(
          THIRST,
          CapabilitySerializer.serializeCapabilityToData(TANCapabilities.THIRST, nativePlayer));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<DataView> temperature = container.getView(TEMPERATURE);
      Optional<DataView> thirst = container.getView(THIRST);

      if (temperature.isPresent() && thirst.isPresent()) {
        final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);

        CapabilitySerializer.deserializeCapabilityFromData(
            TANCapabilities.TEMPERATURE, nativePlayer, temperature.get());
        CapabilitySerializer.deserializeCapabilityFromData(
            TANCapabilities.THIRST, nativePlayer, thirst.get());
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\ttemperature:\t" + temperature.isPresent());
        getLogger().info("\t\t\tthirst:\t" + thirst.isPresent());
      }
    }
  }
}

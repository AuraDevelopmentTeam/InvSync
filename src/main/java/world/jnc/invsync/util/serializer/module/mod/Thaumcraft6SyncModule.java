package world.jnc.invsync.util.serializer.module.mod;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import world.jnc.invsync.util.serializer.CapabilitySerializer;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class Thaumcraft6SyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "thaumcraft";
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
      justification = "Capabilities aren't null during runtime (but compile time).")
  @UtilityClass
  private static class Helper {
    private static final DataQuery KNOWLEDGE = DataQuery.of("knowledge");
    private static final DataQuery WARP = DataQuery.of("warp");

    private static DataView serialize(Player player, DataView container) {
      container.set(
          KNOWLEDGE,
          CapabilitySerializer.serializeCapabilityToData(
              ThaumcraftCapabilities.KNOWLEDGE, NativeInventorySerializer.getNativePlayer(player)));
      container.set(
          WARP,
          CapabilitySerializer.serializeCapabilityToData(
              ThaumcraftCapabilities.WARP, NativeInventorySerializer.getNativePlayer(player)));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<DataView> knowledge = container.getView(KNOWLEDGE);
      Optional<DataView> warp = container.getView(WARP);

      if (knowledge.isPresent() && warp.isPresent()) {
        CapabilitySerializer.deserializeCapabilityFromData(
            ThaumcraftCapabilities.KNOWLEDGE,
            NativeInventorySerializer.getNativePlayer(player),
            knowledge.get());
        CapabilitySerializer.deserializeCapabilityFromData(
            ThaumcraftCapabilities.WARP,
            NativeInventorySerializer.getNativePlayer(player),
            warp.get());
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tknowledge:\t" + knowledge.isPresent());
        getLogger().info("\t\t\twarp:\t" + warp.isPresent());
      }
    }
  }
}

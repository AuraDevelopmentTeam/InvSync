package world.jnc.invsync.util.serializer.module.mod;

import com.cazsius.solcarrot.api.SOLCarrotAPI;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.util.serializer.CapabilitySerializer;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class SolCarrotSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "solcarrot";
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
      final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);

      container.set(
          THIS,
          CapabilitySerializer.serializeCapabilityToData(
              SOLCarrotAPI.FOOD_CAPABILITY, nativePlayer));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<?> serializedFoods = container.get(THIS);
      int foodCount = 0;

      if (serializedFoods.isPresent()) {
        final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);

        CapabilitySerializer.deserializeCapabilityFromData(
            SOLCarrotAPI.FOOD_CAPABILITY, nativePlayer, serializedFoods.get());

        SOLCarrotAPI.syncFoodList(nativePlayer);

        foodCount =
            nativePlayer.getCapability(SOLCarrotAPI.FOOD_CAPABILITY, null).getEatenFoodCount();
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tserializedFoods:\t" + serializedFoods.isPresent());
        if (serializedFoods.isPresent()) getLogger().info("\t\t\tfoodCount:\t" + foodCount);
      }
    }
  }
}

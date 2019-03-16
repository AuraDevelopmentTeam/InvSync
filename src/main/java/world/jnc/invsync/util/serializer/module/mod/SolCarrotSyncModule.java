package world.jnc.invsync.util.serializer.module.mod;

import com.cazsius.solcarrot.capability.FoodCapability;
import com.cazsius.solcarrot.handler.CapabilityHandler;
import com.cazsius.solcarrot.handler.MaxHealthHandler;
import java.util.List;
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
          CapabilitySerializer.serializeCapabilityToViewList(
              FoodCapability.FOOD_CAPABILITY, nativePlayer));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<List<DataView>> serializedFoods = container.getViewList(THIS);
      int foodCount = 0;

      if (serializedFoods.isPresent()) {
        final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);

        CapabilitySerializer.deserializeCapabilityFromViewList(
            FoodCapability.FOOD_CAPABILITY, nativePlayer, serializedFoods.get());

        final FoodCapability foodCapability =
            nativePlayer.getCapability(FoodCapability.FOOD_CAPABILITY, null);

        if (foodCapability != null) {
          CapabilityHandler.syncFoodList(nativePlayer);

          // Now that the food list has been synchronized, use it to set the player's max health.
          MaxHealthHandler.updateFoodHPModifier(nativePlayer);

          foodCount = foodCapability.getFoodCount();
        }
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tserializedFoods:\t" + serializedFoods.isPresent());
        if (serializedFoods.isPresent()) getLogger().info("\t\t\tfoodCount:\t" + foodCount);
      }
    }
  }
}

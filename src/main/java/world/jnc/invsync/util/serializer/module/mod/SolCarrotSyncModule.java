package world.jnc.invsync.util.serializer.module.mod;

import com.cazsius.solcarrot.capability.FoodCapability;
import com.cazsius.solcarrot.capability.FoodInstance;
import com.cazsius.solcarrot.handler.HandlerCapability;
import com.cazsius.solcarrot.handler.MaxHealthHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
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
    private static final DataQuery STACK = DataQuery.of("stack");
    private static final DataQuery META = DataQuery.of("meta");

    private static DataView serialize(Player player, DataView container) {
      FoodCapability foodCapability =
          NativeInventorySerializer.getNativePlayer(player)
              .getCapability(FoodCapability.FOOD_CAPABILITY, null);
      List<DataView> serializedFoods = new LinkedList<>();

      if (foodCapability != null) {
        for (FoodInstance food : foodCapability.getHistory()) {
          DataContainer serializedFood =
              DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
          serializedFood.set(
              STACK, ItemStackUtil.fromNative(new net.minecraft.item.ItemStack(food.item())));
          serializedFood.set(META, food.meta());
          serializedFoods.add(serializedFood);
        }
      }

      container.set(THIS, serializedFoods);

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<List<DataView>> serializedFoods = container.getViewList(THIS);

      if (serializedFoods.isPresent()) {
        // Deserialize to a new instance first. This way if anything goes wrong, an exception is
        // raised before
        // the real food list is cleared.
        FoodCapability foodCapability = new FoodCapability();
        for (DataView serializedFood : serializedFoods.get()) {
          ItemStack food_stack =
              ItemStack.builder().fromContainer(serializedFood.getView(STACK).get()).build();
          int meta = serializedFood.getInt(META).get();
          Item food_item = (Item) food_stack.getType();

          foodCapability.addFood(food_item, meta);
        }

        if (getDebug()) {
          getLogger().info("\t\tisPresent:");
          getLogger().info("\t\t\tserializedFoods:\t" + serializedFoods.isPresent());
        }

        // Now copy the food list to the player
        EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);
        FoodCapability playerFoodCapability =
            nativePlayer.getCapability(FoodCapability.FOOD_CAPABILITY, null);

        if (playerFoodCapability != null) {
          playerFoodCapability.copyFoods(foodCapability);
          HandlerCapability.syncFoodList(nativePlayer);

          // Now that the food list has been synchronized, use it to set the player's max health.
          MaxHealthHandler.updateFoodHPModifier(nativePlayer);

          if (getDebug()) getLogger().info("\t\t\tfoodCount:\t" + foodCapability.getCount());
        }
      }
    }
  }
}

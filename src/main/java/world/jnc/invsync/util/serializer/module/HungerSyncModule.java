package world.jnc.invsync.util.serializer.module;

import java.util.Optional;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;

public class HungerSyncModule extends BaseSyncModule {
  private static final DataQuery FOOD_LEVEL = DataQuery.of("foodLevel");
  private static final DataQuery SATURATION = DataQuery.of("saturation");

  private static final Key<MutableBoundedValue<Integer>> KEY_FOOD_LEVEL = Keys.FOOD_LEVEL;
  private static final Key<MutableBoundedValue<Double>> KEY_SATURATION = Keys.SATURATION;

  @Override
  public String getName() {
    return "hunger";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(FOOD_LEVEL, player.get(KEY_FOOD_LEVEL).get());
    container.set(SATURATION, player.get(KEY_SATURATION).get());

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<Integer> foodLevel = container.getInt(FOOD_LEVEL);
    Optional<Double> saturation = container.getDouble(SATURATION);

    if (foodLevel.isPresent() && saturation.isPresent()) {
      player.offer(KEY_FOOD_LEVEL, foodLevel.get());
      player.offer(KEY_SATURATION, saturation.get());
    }

    // TODO: Debug Logging
  }
}

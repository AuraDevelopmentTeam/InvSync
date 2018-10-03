package world.jnc.invsync.util.serializer.module;

import java.util.Optional;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;

public class HealthSyncModule extends BaseSyncModule {
  private static final Key<MutableBoundedValue<Double>> KEY_HEALTH = Keys.HEALTH;

  @Override
  public String getName() {
    return "health";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(THIS, player.get(KEY_HEALTH).get());

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<Double> health = container.getDouble(THIS);

    if (health.isPresent()) {
      player.offer(KEY_HEALTH, health.get());
    }

    // TODO: Debug Logging
  }
}

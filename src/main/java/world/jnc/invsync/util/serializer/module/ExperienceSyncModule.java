package world.jnc.invsync.util.serializer.module;

import java.util.Optional;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;

public class ExperienceSyncModule extends BaseSyncModule {
  private static final DataQuery EXPERIENCE_LEVEL = DataQuery.of("experience_level");
  private static final DataQuery EXPERIENCE_SINCE_LEVEL = DataQuery.of("experience_since_level");

  private static final Key<MutableBoundedValue<Integer>> KEY_EXPERIENCE_LEVEL =
      Keys.EXPERIENCE_LEVEL;
  private static final Key<MutableBoundedValue<Integer>> KEY_EXPERIENCE_SINCE_LEVEL =
      Keys.EXPERIENCE_SINCE_LEVEL;

  @Override
  public String getName() {
    return "experience";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(EXPERIENCE_LEVEL, player.get(KEY_EXPERIENCE_LEVEL).get());
    container.set(EXPERIENCE_SINCE_LEVEL, player.get(KEY_EXPERIENCE_SINCE_LEVEL).get());

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<Integer> experience_level = container.getInt(EXPERIENCE_LEVEL);
    Optional<Integer> experience_since_level = container.getInt(EXPERIENCE_SINCE_LEVEL);

    if (experience_level.isPresent() && experience_since_level.isPresent()) {
      player.offer(KEY_EXPERIENCE_LEVEL, experience_level.get());
      player.offer(KEY_EXPERIENCE_SINCE_LEVEL, experience_since_level.get());
    }

    // TODO: Debug Logging
  }
}

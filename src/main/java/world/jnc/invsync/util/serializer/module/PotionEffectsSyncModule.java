package world.jnc.invsync.util.serializer.module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;

public class PotionEffectsSyncModule extends BaseSyncModule {
  private static final Key<ListValue<PotionEffect>> KEY_POTION_EFFECTS = Keys.POTION_EFFECTS;

  @Override
  public String getName() {
    return "potion_effects";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(THIS, player.get(KEY_POTION_EFFECTS).orElse(Collections.emptyList()));

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<List<PotionEffect>> potionEffects =
        container.getSerializableList(THIS, PotionEffect.class);

    if (potionEffects.isPresent()) {
      player.offer(KEY_POTION_EFFECTS, potionEffects.get());
    }

    // TODO: Debug Logging
  }
}

package world.jnc.invsync.util.serializer.module;

import java.util.Optional;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;

public class GameModeSyncModule extends BaseSyncModule {
  private static final Key<Value<GameMode>> KEY_GAME_MODE = Keys.GAME_MODE;

  @Override
  public String getName() {
    return "game_mode";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    container.set(THIS, player.get(KEY_GAME_MODE).get());

    return container;
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Optional<GameMode> gameMode = container.getCatalogType(THIS, GameMode.class);

    if (gameMode.isPresent()) {
      player.offer(KEY_GAME_MODE, gameMode.get());
    }

    // TODO: Debug Logging
  }
}

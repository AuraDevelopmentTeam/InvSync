package world.jnc.invsync.util.serializer.module.mod;

import de.eydamos.backpack.data.PlayerSave;
import de.eydamos.backpack.misc.Constants;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.util.serializer.CapabilitySerializer;

public class BackpacksSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "backpack";
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
      final PlayerSave playerSave = getPlayerSave(player);
      final NBTTagCompound nbt = playerSave.serializeNBT();

      container.set(THIS, CapabilitySerializer.nbtToData(nbt));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<Object> serializedNbt = container.getObject(THIS, Object.class);

      if (serializedNbt.isPresent()) {
        final PlayerSave playerSave = getPlayerSave(player);

        playerSave.deserializeNBT(
            (NBTTagCompound) CapabilitySerializer.dataToNbt(serializedNbt.get()));
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tserializedNbt:\t" + serializedNbt.isPresent());
      }
    }

    private static PlayerSave getPlayerSave(Player player) {
      return new PlayerSave(getPlayerPath(player));
    }

    private static String getPlayerPath(Player player) {
      return Constants.PLAYERS_PATH + player.getUniqueId().toString();
    }
  }
}

package world.jnc.invsync.util.serializer.module.mod;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.common.data.persistence.NbtTranslator;
import toughasnails.api.TANCapabilities;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class ToughAsNailsSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "toughasnails";
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
    private static final DataQuery TEMPERATURE = DataQuery.of("temperature");
    private static final DataQuery THIRST = DataQuery.of("thirst");

    private static DataView serialize(Player player, DataView container) {
      final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);
      final NbtTranslator nbtTranslator = NbtTranslator.getInstance();

      container.set(
          TEMPERATURE, nbtTranslator.translate(getNBT(TANCapabilities.TEMPERATURE, nativePlayer)));
      container.set(THIRST, nbtTranslator.translate(getNBT(TANCapabilities.THIRST, nativePlayer)));

      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<DataView> temperature = container.getView(TEMPERATURE);
      Optional<DataView> thirst = container.getView(THIRST);

      if (temperature.isPresent() && thirst.isPresent()) {
        final EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);
        final NbtTranslator nbtTranslator = NbtTranslator.getInstance();

        setNBT(
            TANCapabilities.TEMPERATURE, nativePlayer, nbtTranslator.translate(temperature.get()));
        setNBT(TANCapabilities.THIRST, nativePlayer, nbtTranslator.translate(thirst.get()));
      }

      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\ttemperature:\t" + temperature.isPresent());
        getLogger().info("\t\t\tthirst:\t" + thirst.isPresent());
      }
    }

    private static <T> NBTTagCompound getNBT(Capability<T> capability, EntityPlayer player) {
      return (NBTTagCompound) capability.writeNBT(player.getCapability(capability, null), null);
    }

    private static <T> void setNBT(
        Capability<T> capability, EntityPlayer player, NBTTagCompound nbt) {
      capability.readNBT(player.getCapability(capability, null), null, nbt);
    }
  }
}

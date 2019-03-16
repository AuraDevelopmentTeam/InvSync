package world.jnc.invsync.util.serializer;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.DataView.SafetyMode;
import org.spongepowered.common.data.persistence.NbtTranslator;

// TODO: Testing!!
@UtilityClass
public class CapabilitySerializer {
  private static final String OUTER = "xxx";
  private static final DataQuery OUTER_QUERY = DataQuery.of(OUTER);

  public static <T> NBTBase serializeCapability(Capability<T> capability, EntityPlayer player) {
    return capability.writeNBT(player.getCapability(capability, null), null);
  }

  public static <T> Object serializeCapabilityToView(
      Capability<T> capability, EntityPlayer player) {
    final NBTTagCompound container = new NBTTagCompound();
    container.setTag(OUTER, serializeCapability(capability, player));

    return NbtTranslator.getInstance().translateFrom(container).get(OUTER_QUERY);
  }

  public static <T> void deserializeCapability(
      Capability<T> capability, EntityPlayer player, NBTBase nbt) {
    capability.readNBT(player.getCapability(capability, null), null, nbt);
  }

  public static <T> void deserializeCapabilityFromView(
      Capability<T> capability, EntityPlayer player, Object data) {
    final DataView container = DataContainer.createNew(SafetyMode.NO_DATA_CLONED);
    container.set(OUTER_QUERY, data);

    deserializeCapability(
        capability, player, NbtTranslator.getInstance().translateData(container).getTag(OUTER));
  }
}

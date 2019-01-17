package world.jnc.invsync.util.serializer;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.api.data.DataView;
import org.spongepowered.common.data.persistence.NbtTranslator;

@UtilityClass
public class CapabilitySerializer {
  public static <T> NBTTagCompound serializeCapability(
      Capability<T> capability, EntityPlayer player) {
    return (NBTTagCompound) capability.writeNBT(player.getCapability(capability, null), null);
  }

  public static <T> DataView serializeCapabilityToView(
      Capability<T> capability, EntityPlayer player) {
    return NbtTranslator.getInstance().translate(serializeCapability(capability, player));
  }

  public static <T> void deserializeCapability(
      Capability<T> capability, EntityPlayer player, NBTTagCompound nbt) {
    capability.readNBT(player.getCapability(capability, null), null, nbt);
  }

  public static <T> void deserializeCapabilityFromView(
      Capability<T> capability, EntityPlayer player, DataView view) {
    deserializeCapability(capability, player, NbtTranslator.getInstance().translate(view));
  }
}

package world.jnc.invsync.util.serializer;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import org.spongepowered.api.data.DataView;
import org.spongepowered.common.data.persistence.NbtTranslator;

// TODO: Testing!!
@UtilityClass
public class CapabilitySerializer {
  public static <T> NBTTagCompound serializeCapability(
      Capability<T> capability, EntityPlayer player) {
    return (NBTTagCompound) capability.writeNBT(player.getCapability(capability, null), null);
  }

  public static <T> DataView serializeCapabilityToView(
      Capability<T> capability, EntityPlayer player) {
    return NbtTranslator.getInstance().translateFrom(serializeCapability(capability, player));
  }

  public static <T> NBTTagList serializeCapabilityList(
      Capability<T> capability, EntityPlayer player) {
    return (NBTTagList) capability.writeNBT(player.getCapability(capability, null), null);
  }

  public static <T> List<DataView> serializeCapabilityToViewList(
      Capability<T> capability, EntityPlayer player) {
    final NbtTranslator translator = NbtTranslator.getInstance();

    return StreamSupport.stream(serializeCapabilityList(capability, player).spliterator(), false)
        .map(NBTTagCompound.class::cast)
        .map(translator::translateFrom)
        .collect(Collectors.toList());
  }

  public static <T> void deserializeCapability(
      Capability<T> capability, EntityPlayer player, NBTTagCompound nbt) {
    capability.readNBT(player.getCapability(capability, null), null, nbt);
  }

  public static <T> void deserializeCapabilityFromView(
      Capability<T> capability, EntityPlayer player, DataView view) {
    deserializeCapability(capability, player, NbtTranslator.getInstance().translateData(view));
  }

  public static <T> void deserializeCapabilityList(
      Capability<T> capability, EntityPlayer player, NBTTagList nbt) {
    capability.readNBT(player.getCapability(capability, null), null, nbt);
  }

  public static <T> void deserializeCapabilityFromViewList(
      Capability<T> capability, EntityPlayer player, List<DataView> view) {
    final NbtTranslator translator = NbtTranslator.getInstance();

    deserializeCapabilityList(
        capability,
        player,
        view.stream()
            .map(translator::translateData)
            .collect(Collector.of(NBTTagList::new, NBTTagList::appendTag, null)));
  }
}

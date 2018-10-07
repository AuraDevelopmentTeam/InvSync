package world.jnc.invsync.util.serializer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.DataView.SafetyMode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

@UtilityClass
public class NativeInventorySerializer {
  private static final DataQuery SLOT = InventorySerializer.SLOT;
  private static final DataQuery STACK = InventorySerializer.STACK;

  @SuppressFBWarnings(
    value = "BC_UNCONFIRMED_CAST",
    justification =
        "Player must be a EntityPlayer.\n"
            + "If this is not the case, it's a good thing this blows up with an Error rather than an exception."
  )
  public static EntityPlayer getNativePlayer(Player player) {
    return (EntityPlayer) player;
  }

  public static List<DataView> serializeInventory(IItemHandlerModifiable inventory) {
    DataContainer container;
    List<DataView> slots = new LinkedList<>();

    ItemStack stack;

    for (int i = 0; i < inventory.getSlots(); ++i) {
      stack = inventory.getStackInSlot(i);

      if (!stack.isEmpty()) {
        container = DataContainer.createNew(SafetyMode.ALL_DATA_CLONED);

        container.set(SLOT, i);
        container.set(STACK, serializeItemStack(stack));

        slots.add(container);
      }
    }

    return slots;
  }

  public static boolean deserializeInventory(
      List<DataView> slots, IItemHandlerModifiable inventory) {
    Map<Integer, ItemStack> stacks = new HashMap<>();
    int i;
    ItemStack stack;
    boolean fail = false;

    for (DataView slot : slots) {
      i = slot.getInt(SLOT).get();
      stack = deserializeItemStack(slot.getView(STACK).get());

      stacks.put(i, stack);
    }

    for (i = 0; i < inventory.getSlots(); ++i) {
      if (stacks.containsKey(i)) {
        try {
          inventory.setStackInSlot(i, stacks.get(i));
        } catch (NoSuchElementException e) {
          inventory.setStackInSlot(i, ItemStack.EMPTY);

          fail = true;
        }
      } else {
      }
    }

    return fail;
  }

  static DataView serializeItemStack(ItemStack item) {
    return InventorySerializer.serializeItemStack(ItemStackUtil.fromNative(item));
  }

  static ItemStack deserializeItemStack(DataView data) {
    return ItemStackUtil.toNative(InventorySerializer.deserializeItemStack(data));
  }
}

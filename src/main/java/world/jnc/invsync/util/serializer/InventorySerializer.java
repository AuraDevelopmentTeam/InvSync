package world.jnc.invsync.util.serializer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InventorySerializer {
	private static final DataQuery SLOT = DataQuery.of("slot");
	private static final DataQuery STACK = DataQuery.of("stack");

	// TODO: Remove MemoryDataContainer when API 5.x.x is no longer in use
	@SuppressWarnings("deprecation")
	public static List<DataView> serializeInventory(Inventory inventory) {
		DataContainer container;
		List<DataView> slots = new LinkedList<>();

		int i = 0;
		Optional<ItemStack> stack;

		for (Inventory inv : inventory.slots()) {
			stack = inv.peek();

			if (stack.isPresent()) {
				container = new org.spongepowered.api.data.MemoryDataContainer();

				container.set(SLOT, i);
				container.set(STACK, serializeItemStack(stack.get()));

				slots.add(container);
			}

			i++;
		}

		return slots;
	}

	public static void deserializeInventory(List<DataView> slots, Inventory inventory) {
		Map<Integer, ItemStack> stacks = new HashMap<>();
		int i;
		ItemStack stack;

		for (DataView slot : slots) {
			i = slot.getInt(SLOT).get();
			stack = deserializeItemStack(slot.getView(STACK).get());

			stacks.put(i, stack);
		}

		i = 0;

		for (Inventory slot : inventory.slots()) {
			if (stacks.containsKey(i)) {
				slot.set(stacks.get(i));
			} else {
				slot.clear();
			}

			++i;
		}
	}

	private static DataView serializeItemStack(ItemStack item) {
		return item.toContainer();
	}

	private static ItemStack deserializeItemStack(DataView data) {
		return ItemStack.builder().fromContainer(data).build();
	}
}

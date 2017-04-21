package world.jnc.invsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Optional;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import world.jnc.invsync.InventorySync;

@UtilityClass
public class InventorySerializer {
	public static byte[] serializeInventory(Inventory inventory) throws IOException {
		@Cleanup
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		@Cleanup
		ObjectOutputStream objOut = new ObjectOutputStream(out);

		int i = 0;
		Optional<ItemStack> stack;

		for (Inventory inv : inventory.slots()) {
			stack = inv.peek();

			if (stack.isPresent()) {
				try {
					objOut.writeInt(i);
					writeItemStack(stack.get(), objOut);
				} catch (IOException e) {
					InventorySync.getLogger().error("Error while serializing inventory", e);
				}
			}

			i++;
		}

		objOut.close();

		return out.toByteArray();
	}

	public static void deserializeInventory(byte[] data, Inventory inventory)
			throws IOException, ClassNotFoundException {
		@Cleanup
		ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(data));

		int i = 0;
		int nextIndex;
		ItemStack nextStack;
		Iterator<Inventory> slotIt = inventory.slots().iterator();
		Inventory slot = slotIt.next();

		while (true) {
			try {
				nextIndex = objIn.readInt();
			} catch (EOFException e) {
				break;
			}

			nextStack = getNextItemStack(objIn);

			while (i != nextIndex) {
				slot.clear();

				slot = slotIt.next();
				++i;
			}

			slot.set(nextStack);
		}
	}

	private static void writeItemStack(ItemStack stack, ObjectOutputStream objOut) throws IOException {
		objOut.writeObject(stack.toContainer().toString());

		for (DataQuery query : stack.toContainer().getKeys(false)) {
			InventorySync.getLogger().info(query.toString());
		}

		for (DataQuery query : stack.toContainer().getKeys(true)) {
			InventorySync.getLogger().info(query.toString());
		}
	}

	private static ItemStack getNextItemStack(ObjectInputStream objIn) throws IOException, ClassNotFoundException {
		return ItemStack.builder().fromContainer((MemoryDataContainer) objIn.readObject()).build();
	}
}

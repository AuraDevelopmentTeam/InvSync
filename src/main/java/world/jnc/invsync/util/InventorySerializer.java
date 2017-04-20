package world.jnc.invsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
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
		Version version = Version.V1;

		objOut.writeObject(version.name());

		for (Inventory inv : inventory.slots()) {
			stack = inv.peek();

			if (stack.isPresent()) {
				try {
					objOut.writeInt(i);
					writeItemStack(stack.get(), objOut, version);
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
		Version version = Version.valueOf((String) objIn.readObject());

		while (true) {
			try {
				nextIndex = objIn.readInt();
			} catch (EOFException e) {
				break;
			}
			
			nextStack = getNextItemStack(objIn, version);

			while (i != nextIndex) {
				slot.clear();

				slot = slotIt.next();
				++i;
			}

			slot.set(nextStack);
		}
	}

	private static void writeItemStack(ItemStack stack, ObjectOutputStream objOut, Version version) throws IOException {
		if (version == Version.V1) {
			objOut.writeObject(stack.getItem().getId());
			objOut.writeInt(stack.getQuantity());
		} else {
			throw new UnsupportedOperationException("Unsupported serialization version: " + version);
		}
	}

	private static ItemStack getNextItemStack(ObjectInputStream objIn, Version version)
			throws IOException, ClassNotFoundException {
		if (version == Version.V1) {
			ItemType type = Sponge.getGame().getRegistry().getType(ItemType.class, (String) objIn.readObject()).get();
			int amount = objIn.readInt();

			return ItemStack.of(type, amount);
		} else {
			throw new UnsupportedOperationException("Unsupported serialization version: " + version);
		}
	}

	private enum Version {
		V1
	}
}

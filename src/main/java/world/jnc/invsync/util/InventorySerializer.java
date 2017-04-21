package world.jnc.invsync.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Optional;

import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import com.google.common.reflect.TypeToken;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
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
					objOut.writeObject(serializeItemStack(stack.get()).get());
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

			nextStack = deserializeItemStack((String) objIn.readObject()).get();

			while (i != nextIndex) {
				slot.clear();

				slot = slotIt.next();
				++i;
			}

			slot.set(nextStack);
		}
	}

	private static Optional<String> serializeItemStack(ItemStack item) {
		// try {
		// ConfigurationNode configNode = SimpleConfigurationNode.root();
		//
		// configNode.getNode("myItem").setValue(TypeToken.of(ItemStack.class),
		// item);
		//
		// return Optional.of(configNode.toString());
		// } catch (ObjectMappingException e) {
		// e.printStackTrace();
		// return Optional.empty();
		// }

		try {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink))
					.build();
			ConfigurationNode node = loader.createEmptyNode();
			node.setValue(TypeToken.of(ItemStack.class), item);
			loader.save(node);
			return Optional.of(sink.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private static Optional<ItemStack> deserializeItemStack(String json) {
		// ItemStack item =
		// configNode.getNode("myItem").getValue(TypeToken.of(ItemStack.class));

		try {
			StringReader source = new StringReader(json);
			GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
					.setSource(() -> new BufferedReader(source)).build();
			ConfigurationNode node = loader.load();
			return Optional.of(node.getValue(TypeToken.of(ItemStack.class)));
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}

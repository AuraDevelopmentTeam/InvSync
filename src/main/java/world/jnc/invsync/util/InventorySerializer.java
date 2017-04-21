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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.DataFormatException;

import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import world.jnc.invsync.InventorySync;

@UtilityClass
public class InventorySerializer {
	private static final DataTranslator<ConfigurationNode> CONFIGURATION_NODE = DataTranslators.CONFIGURATION_NODE;

	public static byte[] serializeInventory(Inventory inventory) throws IOException, DataFormatException {
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

		return CompressionUtils.compress(out.toByteArray());
	}

	public static void deserializeInventory(byte[] data, Inventory inventory)
			throws IOException, ClassNotFoundException, DataFormatException {
		@Cleanup
		ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(CompressionUtils.decompress(data)));

		Map<Integer, ItemStack> stacks = new HashMap<>();
		int i;
		ItemStack stack;

		while (true) {
			try {
				i = objIn.readInt();
				stack = deserializeItemStack((String) objIn.readObject()).get();

				stacks.put(i, stack);
			} catch (EOFException e) {
				break;
			}
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

	private static Optional<String> serializeItemStack(ItemStack item) {
		try {
			StringWriter sink = new StringWriter();
			GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink))
					.setIndent(0).build();
			ConfigurationNode node = CONFIGURATION_NODE.translate(item.toContainer());
			loader.save(node);
			return Optional.of(sink.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private static Optional<ItemStack> deserializeItemStack(String json) {
		try {
			StringReader source = new StringReader(json);
			GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
					.setSource(() -> new BufferedReader(source)).build();
			ConfigurationNode node = loader.load();
			// TODO Fix Enchantments and stuff!
			return Optional.of(ItemStack.builder().fromContainer(CONFIGURATION_NODE.translate(node)).build());
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}

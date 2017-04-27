package world.jnc.invsync.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;

@UtilityClass
public class InventorySerializer {
	private static final DataQuery INVENTORY = DataQuery.of("inventory");
	private static final DataQuery ENDER_CHEST = DataQuery.of("enderChest");
	private static final DataQuery GAME_MODE = DataQuery.of("gameMode");
	private static final DataQuery EXPERIENCE = DataQuery.of("experience");
	private static final DataQuery HEALTH = DataQuery.of("health");
	private static final DataQuery FOOD_LEVEL = DataQuery.of("foodLevel");
	private static final DataQuery SATURATION = DataQuery.of("saturation");
	private static final DataQuery SLOT = DataQuery.of("slot");
	private static final DataQuery STACK = DataQuery.of("stack");

	private static final Key<Value<GameMode>> KEY_GAME_MODE = Keys.GAME_MODE;
	private static final Key<MutableBoundedValue<Integer>> KEY_EXPERIENCE = Keys.TOTAL_EXPERIENCE;
	private static final Key<MutableBoundedValue<Double>> KEY_HEALTH = Keys.HEALTH;
	private static final Key<MutableBoundedValue<Integer>> KEY_FOOD_LEVEL = Keys.FOOD_LEVEL;
	private static final Key<MutableBoundedValue<Double>> KEY_SATURATION = Keys.SATURATION;

	public static byte[] serializePlayer(Player player) throws IOException {
		DataContainer container = new MemoryDataContainer();

		if (Config.Values.Synchronize.getEnableInventory()) {
			container.set(INVENTORY, serializeInventory(player.getInventory()));
		}
		if (Config.Values.Synchronize.getEnableEnderChest()) {
			container.set(ENDER_CHEST, serializeInventory(player.getEnderChestInventory()));
		}
		if (Config.Values.Synchronize.getEnableGameMode()) {
			container.set(GAME_MODE, player.get(KEY_GAME_MODE).get());
		}
		if (Config.Values.Synchronize.getEnableExperience()) {
			container.set(EXPERIENCE, player.get(KEY_EXPERIENCE).get());
		}
		if (Config.Values.Synchronize.getEnableHealth()) {
			container.set(HEALTH, player.get(KEY_HEALTH).get());
		}
		if (Config.Values.Synchronize.getEnableHunger()) {
			container.set(FOOD_LEVEL, player.get(KEY_FOOD_LEVEL).get());
			container.set(SATURATION, player.get(KEY_SATURATION).get());
		}
		if (Config.Values.Global.getDebug()) {
			@Cleanup
			ByteArrayOutputStream debug = new ByteArrayOutputStream();

			DataFormats.JSON.writeTo(debug, container);

			InventorySync.getLogger().info(debug.toString());
		}

		@Cleanup
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		@Cleanup
		GZIPOutputStream zipOut = new GZIPOutputStream(out) {
			{
				def.setLevel(Deflater.BEST_COMPRESSION);
			}
		};

		DataFormats.NBT.writeTo(zipOut, container);

		zipOut.close();
		return out.toByteArray();
	}

	public static void deserializePlayer(Player player, byte[] data) throws IOException {
		@Cleanup
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		@Cleanup
		GZIPInputStream zipIn = new GZIPInputStream(in);

		DataContainer container = DataFormats.NBT.readFrom(zipIn);

		Optional<List<DataView>> inventory = container.getViewList(INVENTORY);
		Optional<List<DataView>> enderChest = container.getViewList(ENDER_CHEST);
		Optional<String> gameMode = container.getString(GAME_MODE);
		Optional<Integer> experience = container.getInt(EXPERIENCE);
		Optional<Double> health = container.getDouble(HEALTH);
		Optional<Integer> foodLevel = container.getInt(FOOD_LEVEL);
		Optional<Double> saturation = container.getDouble(SATURATION);

		if (inventory.isPresent() && Config.Values.Synchronize.getEnableInventory()) {
			deserializeInventory(inventory.get(), player.getInventory());
		}
		if (enderChest.isPresent() && Config.Values.Synchronize.getEnableEnderChest()) {
			deserializeInventory(enderChest.get(), player.getEnderChestInventory());
		}
		if (gameMode.isPresent() && Config.Values.Synchronize.getEnableGameMode()) {
			player.offer(KEY_GAME_MODE, getGameMode(gameMode.get()));
		}
		if (experience.isPresent() && Config.Values.Synchronize.getEnableExperience()) {
			player.offer(KEY_EXPERIENCE, experience.get());
		}
		if (health.isPresent() && Config.Values.Synchronize.getEnableHealth()) {
			player.offer(KEY_HEALTH, health.get());
		}
		if (foodLevel.isPresent() && saturation.isPresent() && Config.Values.Synchronize.getEnableHunger()) {
			player.offer(KEY_FOOD_LEVEL, foodLevel.get());
			player.offer(KEY_SATURATION, saturation.get());
		}
	}

	private static List<DataView> serializeInventory(Inventory inventory) {
		DataContainer container;
		List<DataView> slots = new LinkedList<>();

		int i = 0;
		Optional<ItemStack> stack;

		for (Inventory inv : inventory.slots()) {
			stack = inv.peek();

			if (stack.isPresent()) {
				container = new MemoryDataContainer();

				container.set(SLOT, i);
				container.set(STACK, serializeItemStack(stack.get()));

				slots.add(container);
			}

			i++;
		}

		return slots;
	}

	private static void deserializeInventory(List<DataView> slots, Inventory inventory) {
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

	private static GameMode getGameMode(String gameMode) {
		return Sponge.getRegistry().getType(GameMode.class, gameMode).orElse(GameModes.SURVIVAL);
	}
}

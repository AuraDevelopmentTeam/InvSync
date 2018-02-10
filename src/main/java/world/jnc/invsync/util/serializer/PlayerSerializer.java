package world.jnc.invsync.util.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.DataView.SafetyMode;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.permission.PermissionRegistry;
import world.jnc.invsync.util.database.DataSource;

@UtilityClass
public class PlayerSerializer {
  private static final DataQuery INVENTORY = DataQuery.of("inventory");
  private static final DataQuery SELECTED_SLOT = DataQuery.of("selectedSlot");
  private static final DataQuery ENDER_CHEST = DataQuery.of("enderChest");
  private static final DataQuery GAME_MODE = DataQuery.of("gameMode");
  private static final DataQuery EXPERIENCE_LEVEL = DataQuery.of("experience_level");
  private static final DataQuery EXPERIENCE_SINCE_LEVEL = DataQuery.of("experience_since_level");
  private static final DataQuery HEALTH = DataQuery.of("health");
  private static final DataQuery FOOD_LEVEL = DataQuery.of("foodLevel");
  private static final DataQuery SATURATION = DataQuery.of("saturation");
  private static final DataQuery POTION_EFFECTS = DataQuery.of("potionEffects");

  private static final Key<Value<GameMode>> KEY_GAME_MODE = Keys.GAME_MODE;
  private static final Key<MutableBoundedValue<Integer>> KEY_EXPERIENCE_LEVEL =
      Keys.EXPERIENCE_LEVEL;
  private static final Key<MutableBoundedValue<Integer>> KEY_EXPERIENCE_SINCE_LEVEL =
      Keys.EXPERIENCE_SINCE_LEVEL;
  private static final Key<MutableBoundedValue<Double>> KEY_HEALTH = Keys.HEALTH;
  private static final Key<MutableBoundedValue<Integer>> KEY_FOOD_LEVEL = Keys.FOOD_LEVEL;
  private static final Key<MutableBoundedValue<Double>> KEY_SATURATION = Keys.SATURATION;
  private static final Key<ListValue<PotionEffect>> KEY_POTION_EFFECTS = Keys.POTION_EFFECTS;

  public static byte[] serializePlayer(Player player) throws IOException {
    DataContainer container = DataContainer.createNew(SafetyMode.ALL_DATA_CLONED);

    if (Config.Values.Synchronize.getEnableInventory()
        && player.hasPermission(PermissionRegistry.SYNC_INVENTORY)) {
      container.set(INVENTORY, InventorySerializer.serializeInventory(player.getInventory()));
      container.set(SELECTED_SLOT, getHotbar(player).getSelectedSlotIndex());
    }
    if (Config.Values.Synchronize.getEnableEnderChest()
        && player.hasPermission(PermissionRegistry.SYNC_ENDER_CHEST)) {
      container.set(
          ENDER_CHEST, InventorySerializer.serializeInventory(player.getEnderChestInventory()));
    }
    if (Config.Values.Synchronize.getEnableGameMode()
        && player.hasPermission(PermissionRegistry.SYNC_GAME_MODE)) {
      container.set(GAME_MODE, player.get(KEY_GAME_MODE).get());
    }
    if (Config.Values.Synchronize.getEnableExperience()
        && player.hasPermission(PermissionRegistry.SYNC_EXPERIENCE)) {
      container.set(EXPERIENCE_LEVEL, player.get(KEY_EXPERIENCE_LEVEL).get());
      container.set(EXPERIENCE_SINCE_LEVEL, player.get(KEY_EXPERIENCE_SINCE_LEVEL).get());
    }
    if (Config.Values.Synchronize.getEnableHealth()
        && player.hasPermission(PermissionRegistry.SYNC_HEALTH)) {
      container.set(HEALTH, player.get(KEY_HEALTH).get());
    }
    if (Config.Values.Synchronize.getEnableHunger()
        && player.hasPermission(PermissionRegistry.SYNC_HUNGER)) {
      container.set(FOOD_LEVEL, player.get(KEY_FOOD_LEVEL).get());
      container.set(SATURATION, player.get(KEY_SATURATION).get());
    }
    if (Config.Values.Synchronize.getEnablePotionEffects()
        && player.hasPermission(PermissionRegistry.SYNC_POTION_EFFECTS)) {
      container.set(POTION_EFFECTS, player.get(KEY_POTION_EFFECTS).orElse(Collections.emptyList()));
    }

    if (Config.Values.Global.getDebug()) {
      printCommonDebugInfo(player, container, true);
    }

    @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
    @Cleanup
    GZIPOutputStream zipOut =
        new GZIPOutputStream(out) {
          {
            def.setLevel(Deflater.BEST_COMPRESSION);
          }
        };

    DataFormats.NBT.writeTo(zipOut, container);

    zipOut.close();
    return out.toByteArray();
  }

  public static void deserializePlayer(Player player, byte[] data) throws IOException {
    @Cleanup ByteArrayInputStream in = new ByteArrayInputStream(data);
    @Cleanup GZIPInputStream zipIn = new GZIPInputStream(in);

    // TODO: Use Cause once SpongeCommon#1355 is fixed
    // Cause cause =
    // Cause.builder().owner(InventorySync.getInstance()).build();
    DataContainer container = DataFormats.NBT.readFrom(zipIn);

    Optional<List<DataView>> inventory = container.getViewList(INVENTORY);
    Optional<Integer> selectedSlot = container.getInt(SELECTED_SLOT);
    Optional<List<DataView>> enderChest = container.getViewList(ENDER_CHEST);
    Optional<GameMode> gameMode = container.getCatalogType(GAME_MODE, GameMode.class);
    Optional<Integer> experience_level = container.getInt(EXPERIENCE_LEVEL);
    Optional<Integer> experience_since_level = container.getInt(EXPERIENCE_SINCE_LEVEL);
    Optional<Double> health = container.getDouble(HEALTH);
    Optional<Integer> foodLevel = container.getInt(FOOD_LEVEL);
    Optional<Double> saturation = container.getDouble(SATURATION);
    Optional<List<PotionEffect>> potionEffects =
        container.getSerializableList(POTION_EFFECTS, PotionEffect.class);

    if (inventory.isPresent()
        && Config.Values.Synchronize.getEnableInventory()
        && player.hasPermission(PermissionRegistry.SYNC_INVENTORY)) {
      boolean fail =
          InventorySerializer.deserializeInventory(inventory.get(), player.getInventory());

      if (selectedSlot.isPresent()) {
        getHotbar(player).setSelectedSlotIndex(selectedSlot.get());
      }

      if (fail) {
        InventorySync.getLogger()
            .error(
                "Could not load inventory of player "
                    + DataSource.getPlayerString(player)
                    + " because there where unknown item.");
        InventorySync.getLogger()
            .warn(
                "Please make sure you are using the same mods on all servers you are synchronizing with.");
      }
    }
    if (enderChest.isPresent()
        && Config.Values.Synchronize.getEnableEnderChest()
        && player.hasPermission(PermissionRegistry.SYNC_ENDER_CHEST)) {
      InventorySerializer.deserializeInventory(enderChest.get(), player.getEnderChestInventory());
    }
    if (gameMode.isPresent()
        && Config.Values.Synchronize.getEnableGameMode()
        && player.hasPermission(PermissionRegistry.SYNC_GAME_MODE)) {
      player.offer(KEY_GAME_MODE, gameMode.get());
    }
    if (experience_level.isPresent()
        && experience_since_level.isPresent()
        && Config.Values.Synchronize.getEnableExperience()
        && player.hasPermission(PermissionRegistry.SYNC_EXPERIENCE)) {
      player.offer(KEY_EXPERIENCE_LEVEL, experience_level.get());
      player.offer(KEY_EXPERIENCE_SINCE_LEVEL, experience_since_level.get());
    }
    if (health.isPresent()
        && Config.Values.Synchronize.getEnableHealth()
        && player.hasPermission(PermissionRegistry.SYNC_HEALTH)) {
      player.offer(KEY_HEALTH, health.get());
    }
    if (foodLevel.isPresent()
        && saturation.isPresent()
        && Config.Values.Synchronize.getEnableHunger()
        && player.hasPermission(PermissionRegistry.SYNC_HUNGER)) {
      player.offer(KEY_FOOD_LEVEL, foodLevel.get());
      player.offer(KEY_SATURATION, saturation.get());
    }
    if (potionEffects.isPresent()
        && Config.Values.Synchronize.getEnablePotionEffects()
        && player.hasPermission(PermissionRegistry.SYNC_POTION_EFFECTS)) {
      player.offer(KEY_POTION_EFFECTS, potionEffects.get());
    }

    if (Config.Values.Global.getDebug()) {
      printCommonDebugInfo(player, container, false);

      Logger logger = InventorySync.getLogger();

      logger.info("Objects:");
      logger.info("inventory.isPresent(): " + inventory.isPresent());
      logger.info("selectedSlot.isPresent(): " + selectedSlot.isPresent());
      logger.info("enderChest.isPresent(): " + enderChest.isPresent());
      logger.info("gameMode.isPresent(): " + gameMode.isPresent());
      logger.info("experience_level.isPresent(): " + experience_level.isPresent());
      logger.info("experience_since_level.isPresent(): " + experience_since_level.isPresent());
      logger.info("health.isPresent(): " + health.isPresent());
      logger.info("foodLevel.isPresent(): " + foodLevel.isPresent());
      logger.info("saturation.isPresent(): " + saturation.isPresent());
      logger.info("potionEffects.isPresent(): " + potionEffects.isPresent());
    }
  }

  private static Hotbar getHotbar(Player player) {
    return ((PlayerInventory) player.getInventory()).getHotbar();
  }

  private static void printCommonDebugInfo(
      Player player, DataContainer container, boolean serializing) throws IOException {
    Logger logger = InventorySync.getLogger();

    if (serializing) {
      logger.info("Serializing data of " + DataSource.getPlayerString(player));
    } else {
      logger.info("Deserializing data of " + DataSource.getPlayerString(player));
    }

    logger.info("Permissions:");
    logger.info(
        PermissionRegistry.SYNC_INVENTORY
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_INVENTORY));
    logger.info(
        PermissionRegistry.SYNC_ENDER_CHEST
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_ENDER_CHEST));
    logger.info(
        PermissionRegistry.SYNC_GAME_MODE
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_GAME_MODE));
    logger.info(
        PermissionRegistry.SYNC_EXPERIENCE
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_EXPERIENCE));
    logger.info(
        PermissionRegistry.SYNC_HEALTH
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_HEALTH));
    logger.info(
        PermissionRegistry.SYNC_HUNGER
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_HUNGER));
    logger.info(
        PermissionRegistry.SYNC_POTION_EFFECTS
            + ": "
            + player.hasPermission(PermissionRegistry.SYNC_POTION_EFFECTS));

    try {
      @Cleanup ByteArrayOutputStream debug = new ByteArrayOutputStream();

      DataFormats.JSON.writeTo(debug, container);

      logger.info(debug.toString());
    } catch (NoSuchFieldError e) {
      // Just a brief message to the user. This happens in API version
      // 5.x.x
      logger.info(
          "You do not use API version 6.x.x or above. Dumping the container data is not available!");
    }
  }
}

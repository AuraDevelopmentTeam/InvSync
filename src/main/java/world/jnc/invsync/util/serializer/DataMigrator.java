package world.jnc.invsync.util.serializer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import world.jnc.invsync.InventorySync;

@UtilityClass
public class DataMigrator {
  public static final DataQuery VERSION_QUERY = DataQuery.of("version");
  public static final int VERSION = 1;

  // Inventory
  private static final DataQuery inventoryV0 = DataQuery.of("inventory");
  private static final DataQuery inventoryV1 = DataQuery.of("inventory", "inventory");

  private static final DataQuery selectedSlotV0 = DataQuery.of("selectedSlot");
  private static final DataQuery selectedSlotV1 = DataQuery.of("inventory", "selectedSlot");

  // Ender Chest
  private static final DataQuery enderChestV0 = DataQuery.of("enderChest");
  private static final DataQuery enderChestV1 = DataQuery.of("ender_chest", "data");

  // GameMode
  private static final DataQuery gameModeV0 = DataQuery.of("gameMode");
  private static final DataQuery gameModeV1 = DataQuery.of("game_mode", "data");

  // Experience
  private static final DataQuery experienceLevelV0 = DataQuery.of("experience_level");
  private static final DataQuery experienceLevelV1 = DataQuery.of("experience", "experience_level");

  private static final DataQuery experienceSinceLevelV0 = DataQuery.of("experience_since_level");
  private static final DataQuery experienceSinceLevelV1 =
      DataQuery.of("experience", "experience_since_level");

  // Health
  private static final DataQuery healthV0 = DataQuery.of("health");
  private static final DataQuery healthV1 = DataQuery.of("health", "data");

  // Hunger
  private static final DataQuery foodLevelV0 = DataQuery.of("foodLevel");
  private static final DataQuery foodLevelV1 = DataQuery.of("hunger", "food_level");

  private static final DataQuery saturationV0 = DataQuery.of("saturation");
  private static final DataQuery saturationV1 = DataQuery.of("hunger", "saturation");

  // Potion Effects
  private static final DataQuery potionEffectsV0 = DataQuery.of("potionEffects");
  private static final DataQuery potionEffectsV1 = DataQuery.of("potion_effects", "data");

  @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH", justification = "Fallthrough is intended")
  public static void migrate(DataView container) throws IOException {
    final int version = getVersion(container);

    final boolean debug = InventorySync.getConfig().getGeneral().getDebug();
    final Logger logger = InventorySync.getLogger();

    if (debug && (version < VERSION)) {
      logger.info("Migrating dataset from version " + version + " to version " + VERSION);
      logger.info("Before:");
      PlayerSerializer.printContainer(container);
    }

    switch (version) {
      case 0: // First version. 0.6.18 and below (doesn't even have a version attribute)
        // Inventory
        moveElement(container, inventoryV0, inventoryV1);
        moveElement(container, selectedSlotV0, selectedSlotV1);

        // Ender Chest
        moveElement(container, enderChestV0, enderChestV1);

        // GameMode
        moveElement(container, gameModeV0, gameModeV1);

        // Experience
        moveElement(container, experienceLevelV0, experienceLevelV1);
        moveElement(container, experienceSinceLevelV0, experienceSinceLevelV1);

        // Health
        moveElement(container, healthV0, healthV1);

        // Hunger
        moveElement(container, foodLevelV0, foodLevelV1);
        moveElement(container, saturationV0, saturationV1);

        // Potion Effects
        moveElement(container, potionEffectsV0, potionEffectsV1);

        // Version
        container.set(VERSION_QUERY, VERSION);

        // Print debug
        if (debug) {
          logger.info("After:");
          PlayerSerializer.printContainer(container);
        }
      case 1: // Current version nothing to do
        break;
      default:
        final String message = "Unknow version \"" + version + "\" while trying to migrate";

        if (InventorySync.getConfig().getGeneral().getDebug()) {
          logger.info(message);
        } else {
          logger.debug(message);
        }
    }
  }

  private static int getVersion(DataView container) {
    return container.getInt(VERSION_QUERY).orElse(0);
  }

  private static void moveElement(DataView container, DataQuery oldQuery, DataQuery newQuery) {
    final Optional<Object> data = container.get(oldQuery);

    if (!data.isPresent())
      // Data might not exist. Just skip this entry
      return;

    container.remove(oldQuery);
    container.set(newQuery, data.get());
  }
}

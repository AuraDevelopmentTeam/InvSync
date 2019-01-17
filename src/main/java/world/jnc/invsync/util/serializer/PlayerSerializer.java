package world.jnc.invsync.util.serializer;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.DataView.SafetyMode;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;
import world.jnc.invsync.util.serializer.module.EnderChestSyncModule;
import world.jnc.invsync.util.serializer.module.ExperienceSyncModule;
import world.jnc.invsync.util.serializer.module.GameModeSyncModule;
import world.jnc.invsync.util.serializer.module.HealthSyncModule;
import world.jnc.invsync.util.serializer.module.HungerSyncModule;
import world.jnc.invsync.util.serializer.module.InventorySyncModule;
import world.jnc.invsync.util.serializer.module.PotionEffectsSyncModule;
import world.jnc.invsync.util.serializer.module.mod.BaublesSyncModule;
import world.jnc.invsync.util.serializer.module.mod.CyclicSyncModule;
import world.jnc.invsync.util.serializer.module.mod.SolCarrotSyncModule;
import world.jnc.invsync.util.serializer.module.mod.SpiceOfLifeSyncModule;
import world.jnc.invsync.util.serializer.module.mod.ToughAsNailsSyncModule;

@UtilityClass
public class PlayerSerializer {
  public static final InventorySyncModule inventorySyncModule = new InventorySyncModule();
  public static final EnderChestSyncModule enderChestSyncModule = new EnderChestSyncModule();
  public static final GameModeSyncModule gameModeSyncModule = new GameModeSyncModule();
  public static final ExperienceSyncModule experienceSyncModule = new ExperienceSyncModule();
  public static final HealthSyncModule healthSyncModule = new HealthSyncModule();
  public static final HungerSyncModule hungerSyncModule = new HungerSyncModule();
  public static final PotionEffectsSyncModule potionEffectsSyncModule =
      new PotionEffectsSyncModule();

  public static final BaublesSyncModule baublesSyncModule = new BaublesSyncModule();
  public static final CyclicSyncModule cyclicSyncModule = new CyclicSyncModule();
  public static final SolCarrotSyncModule solCarrotSyncModule = new SolCarrotSyncModule();
  public static final SpiceOfLifeSyncModule spiceOfLifeSyncModule = new SpiceOfLifeSyncModule();
  public static final ToughAsNailsSyncModule toughAsNailsSyncModule = new ToughAsNailsSyncModule();

  private static final Map<UUID, DataContainer> dataContainerCache = new HashMap<>();

  private static final List<BaseSyncModule> modules =
      new LinkedList<BaseSyncModule>(
          Arrays.asList(
              inventorySyncModule,
              enderChestSyncModule,
              gameModeSyncModule,
              experienceSyncModule,
              // Sync the solcarrot data before syncing the player's current health, because the
              // solcarrot sets the player's maxhealth.
              solCarrotSyncModule,
              healthSyncModule,
              hungerSyncModule,
              potionEffectsSyncModule,
              baublesSyncModule,
              cyclicSyncModule,
              spiceOfLifeSyncModule,
              toughAsNailsSyncModule));
  private static ImmutableList<BaseSyncModule> modulesImmutableListCache = null;

  public static void registerModule(BaseSyncModule module) {
    modules.add(module);
    modulesImmutableListCache = null;
  }

  public static ImmutableList<BaseSyncModule> getModules() {
    if (modulesImmutableListCache == null)
      modulesImmutableListCache = ImmutableList.copyOf(modules);

    return modulesImmutableListCache;
  }

  public static byte[] serializePlayer(Player player, boolean removeFromCache) throws IOException {
    final Config config = InventorySync.getConfig();
    final boolean debug = config.getGeneral().getDebug();
    final Logger logger = InventorySync.getLogger();

    if (debug) {
      logger.info("Serializing data of " + DataSource.getPlayerString(player));
    }

    final DataContainer container = getDataContainer(player, removeFromCache);

    container.set(DataMigrator.VERSION_QUERY, DataMigrator.VERSION);

    for (BaseSyncModule module : modules) {
      if (debug) {
        module.printBasicDebug(player);
      }

      if (module.getSyncPlayer(player)) {
        container.set(
            module.getQuery(), module.serialize(player, container.getView(module.getQuery())));
      }
    }

    if (config.getGeneral().getDebug()) {
      printContainer(container);
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
    final Config config = InventorySync.getConfig();
    final boolean debug = config.getGeneral().getDebug();
    final Logger logger = InventorySync.getLogger();

    if (debug) {
      logger.info("Deserializing data of " + DataSource.getPlayerString(player));
    }

    @Cleanup ByteArrayInputStream in = new ByteArrayInputStream(data);
    @Cleanup GZIPInputStream zipIn = new GZIPInputStream(in);

    // TODO: Use Cause once SpongeCommon#1355 is fixed
    // Cause cause =
    // Cause.builder().owner(InventorySync.getInstance()).build();
    DataContainer container = DataFormats.NBT.readFrom(zipIn);
    dataContainerCache.put(player.getUniqueId(), container);

    DataMigrator.migrate(container);

    for (BaseSyncModule module : modules) {
      if (debug) {
        module.printBasicDebug(player);
      }

      if (module.getSyncPlayer(player)) {
        module.deserialize(player, container.getView(module.getQuery()));
      }
    }

    if (debug) {
      printContainer(container);
    }
  }

  private static DataContainer getDataContainer(Player player, boolean removeFromCache) {
    return getDataContainer(player.getUniqueId(), removeFromCache);
  }

  private static DataContainer getDataContainer(UUID uuid, boolean removeFromCache) {
    if (dataContainerCache.containsKey(uuid)) {
      if (removeFromCache) return dataContainerCache.remove(uuid);
      else return dataContainerCache.get(uuid);
    } else {
      return DataContainer.createNew(SafetyMode.ALL_DATA_CLONED);
    }
  }

  public static void printContainer(DataView container) throws IOException {
    final Logger logger = InventorySync.getLogger();

    try (ByteArrayOutputStream debug = new ByteArrayOutputStream()) {
      DataFormats.JSON.writeTo(debug, container);

      logger.info(debug.toString(StandardCharsets.UTF_8.name()));
    } catch (NoSuchFieldError e) {
      // Just a brief message to the user. This happens in API version
      // 5.x.x
      logger.info(
          "You do not use API version 6.x.x or above. Dumping the container data is not available!");
    } catch (UnsupportedEncodingException e) {
      // Won't happen xD
    } catch (RuntimeException e) {
      // All kinds of shit can happen.
      // We don't want the code to crash while doing debugging!
      // So let's just catch and print the exception!
      logger.warn(
          "An unexpected exception occured while converting the player data to JSON for debugging.\n"
              + "NO actual DATA has been LOST at this point.\n"
              + "You just don't get any debugging output for the player.",
          e);
    }
  }
}

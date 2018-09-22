package world.jnc.invsync.util.metrics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bstats.sponge.Metrics.SimpleBarChart;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;

public class FeatureChart extends SimpleBarChart {
  public static HashMap<String, Integer> getValues() {
    HashMap<String, Integer> sortedMap = new LinkedHashMap<>();

    final Config config = InventorySync.getConfig();
    final Config.Storage storageConfig = config.getStorage();
    final Config.Synchronize synchronizeConfig = config.getSynchronize();

    sortedMap.put("MySQL", storageConfig.isMySQL() ? 1 : 0);
    sortedMap.put("H2", storageConfig.isH2() ? 1 : 0);

    sortedMap.put("SyncInventory", synchronizeConfig.getEnableInventory() ? 1 : 0);
    sortedMap.put("SyncEnderChest", synchronizeConfig.getEnableEnderChest() ? 1 : 0);
    sortedMap.put("SyncGameMode", synchronizeConfig.getEnableGameMode() ? 1 : 0);
    sortedMap.put("SyncExperience", synchronizeConfig.getEnableExperience() ? 1 : 0);
    sortedMap.put("SyncHealth", synchronizeConfig.getEnableHealth() ? 1 : 0);
    sortedMap.put("SyncHunger", synchronizeConfig.getEnableHunger() ? 1 : 0);
    sortedMap.put("SyncPotionEffects", synchronizeConfig.getEnablePotionEffects() ? 1 : 0);

    sortedMap.put("Servers", 1);

    return sortedMap;
  }

  public FeatureChart(String chartId) {
    super(chartId, FeatureChart::getValues);
  }
}

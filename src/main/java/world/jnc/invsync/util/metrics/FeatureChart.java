package world.jnc.invsync.util.metrics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bstats.Metrics.SimpleBarChart;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;

public class FeatureChart extends SimpleBarChart {
  public FeatureChart(String chartId) {
    super(chartId);
  }

  @Override
  public HashMap<String, Integer> getValues(HashMap<String, Integer> premadeMap) {
    HashMap<String, Integer> sortedMap = new LinkedHashMap<>();

    sortedMap.put("MySQL", InventorySync.getDataSource().isMysql() ? 1 : 0);
    sortedMap.put("H2", InventorySync.getDataSource().isH2() ? 1 : 0);

    sortedMap.put("SyncInventory", Config.Values.Synchronize.getEnableInventory() ? 1 : 0);
    sortedMap.put("SyncEnderChest", Config.Values.Synchronize.getEnableEnderChest() ? 1 : 0);
    sortedMap.put("SyncGameMode", Config.Values.Synchronize.getEnableGameMode() ? 1 : 0);
    sortedMap.put("SyncExperience", Config.Values.Synchronize.getEnableExperience() ? 1 : 0);
    sortedMap.put("SyncHealth", Config.Values.Synchronize.getEnableHealth() ? 1 : 0);
    sortedMap.put("SyncHunger", Config.Values.Synchronize.getEnableHunger() ? 1 : 0);
    sortedMap.put("SyncPotionEffects", Config.Values.Synchronize.getEnablePotionEffects() ? 1 : 0);

    sortedMap.put("Servers", 1);

    return sortedMap;
  }
}

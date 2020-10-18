package world.jnc.invsync.util.metrics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bstats.sponge.Metrics2.SimpleBarChart;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.util.serializer.PlayerSerializer;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;

public class FeatureChart extends SimpleBarChart {
  public static HashMap<String, Integer> getValues() {
    HashMap<String, Integer> sortedMap = new LinkedHashMap<>();

    final Config config = InventorySync.getConfig();
    final Config.Storage storageConfig = config.getStorage();

    sortedMap.put("MySQL", storageConfig.isMySQL() ? 1 : 0);
    sortedMap.put("PostgreSQL", storageConfig.isPostgreSQL() ? 1 : 0);
    sortedMap.put("H2", storageConfig.isH2() ? 1 : 0);

    for (BaseSyncModule module : PlayerSerializer.getModules()) {
      sortedMap.put("Sync" + module.getNiceName(), module.isEnabled() ? 1 : 0);
    }

    sortedMap.put("Servers", 1);

    return sortedMap;
  }

  public FeatureChart(String chartId) {
    super(chartId, FeatureChart::getValues);
  }
}

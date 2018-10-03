package world.jnc.invsync.util.metrics;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bstats.sponge.Metrics.SimpleBarChart;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.util.serializer.PlayerSerializer;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;

public class FeatureChart extends SimpleBarChart {
  private static final Pattern capitalizerPattern = Pattern.compile("(?:^|\\.)(\\w)");

  public static HashMap<String, Integer> getValues() {
    HashMap<String, Integer> sortedMap = new LinkedHashMap<>();

    final Config config = InventorySync.getConfig();
    final Config.Storage storageConfig = config.getStorage();

    sortedMap.put("MySQL", storageConfig.isMySQL() ? 1 : 0);
    sortedMap.put("H2", storageConfig.isH2() ? 1 : 0);

    for (BaseSyncModule module : PlayerSerializer.getModules()) {
      sortedMap.put(getNiceModuleName(module), module.isEnabled() ? 1 : 0);
    }

    sortedMap.put("Servers", 1);

    return sortedMap;
  }

  @VisibleForTesting
  static String getNiceModuleName(BaseSyncModule module) {
    final String moduleName = module.getName();
    final StringBuffer buffer = new StringBuffer(moduleName.length());
    final Matcher match = capitalizerPattern.matcher(moduleName);

    while (match.find()) {
      match.appendReplacement(buffer, match.group(1).toUpperCase());
    }

    match.appendTail(buffer);

    return "Sync" + buffer.toString();
  }

  public FeatureChart(String chartId) {
    super(chartId, FeatureChart::getValues);
  }
}

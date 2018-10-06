package world.jnc.invsync.util.metrics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import world.jnc.invsync.util.serializer.module.BaseSyncModule;
import world.jnc.invsync.util.serializer.module.mod.BaseModSyncModule;

public class FeatureChartTest {
  @Test
  public void getNiceModuleNameSimpleTest() {
    final BaseSyncModule module = Mockito.mock(BaseSyncModule.class);
    Mockito.when(module.getName()).thenReturn("inventory");

    assertEquals("SyncInventory", FeatureChart.getNiceModuleName(module));
  }

  @Test
  public void getNiceModuleNameModTest() {
    final BaseModSyncModule module = Mockito.mock(BaseModSyncModule.class);
    Mockito.when(module.getModId()).thenReturn("baubles");
    Mockito.when(module.getName()).thenCallRealMethod();

    assertEquals("SyncModBaubles", FeatureChart.getNiceModuleName(module));
  }

  @Test
  public void getNiceModuleNameGenericTest() {
    final BaseSyncModule module = Mockito.mock(BaseSyncModule.class);
    Mockito.when(module.getName())
        .thenReturn("xUuzbhki")
        .thenReturn("fhiG79.g/ads")
        .thenReturn("1.2");

    assertEquals("SyncXUuzbhki", FeatureChart.getNiceModuleName(module));
    assertEquals("SyncFhiG79G/ads", FeatureChart.getNiceModuleName(module));
    assertEquals("Sync12", FeatureChart.getNiceModuleName(module));
  }
}

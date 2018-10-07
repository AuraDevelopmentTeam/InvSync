package world.jnc.invsync.util.serializer.module;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import world.jnc.invsync.util.serializer.module.mod.BaseModSyncModule;

public class BaseSyncModuleTest {
  @Test
  public void getNiceModuleNameSimpleTest() {
    final BaseSyncModule module = Mockito.mock(BaseSyncModule.class);
    Mockito.when(module.getName()).thenReturn("inventory").thenReturn("ender_chest");
    Mockito.when(module.getNiceName()).thenCallRealMethod();

    assertEquals("Inventory", module.getNiceName());
    assertEquals("EnderChest", module.getNiceName());
  }

  @Test
  public void getNiceModuleNameModTest() {
    final BaseModSyncModule module = Mockito.mock(BaseModSyncModule.class);
    Mockito.when(module.getModId()).thenReturn("baubles").thenReturn("test_mod");
    Mockito.when(module.getName()).thenCallRealMethod();
    Mockito.when(module.getNiceName()).thenCallRealMethod();

    assertEquals("ModBaubles", module.getNiceName());
    assertEquals("ModTestMod", module.getNiceName());
  }

  @Test
  public void getNiceModuleNameGenericTest() {
    final BaseSyncModule module = Mockito.mock(BaseSyncModule.class);
    Mockito.when(module.getName())
        .thenReturn("xUuz_bhki")
        .thenReturn("fh_iG79.g/ads")
        .thenReturn("1.2_3");
    Mockito.when(module.getNiceName()).thenCallRealMethod();

    assertEquals("XUuzBhki", module.getNiceName());
    assertEquals("FhIG79G/ads", module.getNiceName());
    assertEquals("123", module.getNiceName());
  }
}

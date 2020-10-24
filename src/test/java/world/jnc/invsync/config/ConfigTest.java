package world.jnc.invsync.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigTest {
  @Test
  public void urlEncodeTest() {
    assertEquals(
        "test%40%3A%C3%A4%C3%B6%C3%BC%26%3F+%40test",
        Config.Storage.urlEncode("test@:äöü&? @test"));
  }
}

package world.jnc.invsync.util.serializer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;

public class CapabilitySerializerTest {
  @Test
  public void nbtToDataToNbtTest() {
    final NBTTagString nbtString = new NBTTagString("Test");
    final NBTTagByteArray nbtByteArray =
        new NBTTagByteArray("Test".getBytes(StandardCharsets.UTF_8));
    final NBTTagLong nbtLong = new NBTTagLong(1234567890123L);
    final NBTTagList nbtList1 = new NBTTagList();
    nbtList1.appendTag(nbtString);
    nbtList1.appendTag(new NBTTagString("String"));
    final NBTTagCompound nbtCompound1 = new NBTTagCompound();
    nbtCompound1.setTag("string", nbtString);
    nbtCompound1.setTag("byteArray", nbtByteArray);
    nbtCompound1.setTag("long", nbtLong);
    nbtCompound1.setTag("list1", nbtList1);
    final NBTTagCompound nbtCompound2 = new NBTTagCompound();
    nbtCompound2.setTag("string", nbtString);
    nbtCompound2.setTag("byteArray", nbtByteArray);
    nbtCompound2.setTag("long", nbtLong);
    nbtCompound2.setTag("list1", nbtList1);
    nbtCompound2.setTag("compound1", nbtCompound1);
    final NBTTagList nbtList2 = new NBTTagList();
    nbtList2.appendTag(nbtCompound1);
    nbtList2.appendTag(nbtCompound2);
    nbtList2.appendTag(new NBTTagCompound());

    NBTBase[] nbts =
        new NBTBase[] {
          nbtString, nbtByteArray, nbtLong, nbtList1, nbtCompound1, nbtCompound2, nbtList2
        };

    for (NBTBase nbt : nbts) {
      assertEquals(nbt, CapabilitySerializer.dataToNbt(CapabilitySerializer.nbtToData(nbt)));
    }
  }

  @Test
  public void dataToNbtToDataTest() {
    final String dataString = "Test";
    final byte[] dataByteArray = "Test".getBytes(StandardCharsets.UTF_8);
    final long dataLong = 1234567890123L;
    final List<String> dataList1 = Arrays.asList(dataString, "String");
    final DataView dataView1 = DataContainer.createNew();
    dataView1.set(DataQuery.of("string"), dataString);
    // Broken by bug in SpongeAPI. Fix will likely be in 7.2.0
    // See https://github.com/SpongePowered/SpongeAPI/issues/1983
    // dataView1.set(DataQuery.of("byteArray"), dataByteArray);
    dataView1.set(DataQuery.of("long"), dataLong);
    dataView1.set(DataQuery.of("list1"), dataList1);
    final DataView dataView2 = DataContainer.createNew();
    dataView2.set(DataQuery.of("string"), dataString);
    // Broken by bug in SpongeAPI. Fix will likely be in 7.2.0
    // dataView2.set(DataQuery.of("byteArray"), dataByteArray);
    dataView2.set(DataQuery.of("long"), dataLong);
    dataView2.set(DataQuery.of("list1"), dataList1);
    dataView2.set(DataQuery.of("view1"), dataView1);
    final List<DataView> dataList2 = Arrays.asList(dataView1, dataView2, DataContainer.createNew());

    Object[] objects =
        new Object[] {
          dataString, dataByteArray, dataLong, dataList1, dataView1, dataView2, dataList2
        };

    for (Object object : objects) {
      assertEquals(object, CapabilitySerializer.nbtToData(CapabilitySerializer.dataToNbt(object)));
    }
  }

  private static void assertEquals(Object expected, Object actual) {
    final Object expectedCheck =
        (expected instanceof DataView) ? ((DataView) expected).copy() : expected;
    final Object actualCheck = (actual instanceof DataView) ? ((DataView) actual).copy() : actual;

    Assert.assertEquals("Test case: " + expected, expectedCheck, actualCheck);
  }
}

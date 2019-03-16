package world.jnc.invsync.util.serializer;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import org.junit.Test;

public class CapabilitySerializerTest {
  @Test
  public void nbtToDataToNbtTest() {
    final NBTTagString nbtString = new NBTTagString("Test");
    final NBTTagByteArray nbtByteArray =
        new NBTTagByteArray("Test".getBytes(StandardCharsets.UTF_8));
    final NBTTagLong nbtLong = new NBTTagLong(1234567890123L);
    final NBTTagList nbtArray1 = new NBTTagList();
    nbtArray1.appendTag(nbtString);
    nbtArray1.appendTag(new NBTTagString("String"));
    final NBTTagCompound nbtCompound1 = new NBTTagCompound();
    nbtCompound1.setTag("string", nbtString);
    nbtCompound1.setTag("byteArray", nbtByteArray);
    nbtCompound1.setTag("long", nbtLong);
    nbtCompound1.setTag("array1", nbtArray1);
    final NBTTagCompound nbtCompound2 = new NBTTagCompound();
    nbtCompound2.setTag("string", nbtString);
    nbtCompound2.setTag("byteArray", nbtByteArray);
    nbtCompound2.setTag("long", nbtLong);
    nbtCompound2.setTag("array1", nbtArray1);
    nbtCompound2.setTag("compound1", nbtCompound1);
    final NBTTagList nbtArray2 = new NBTTagList();
    nbtArray2.appendTag(nbtCompound1);
    nbtArray2.appendTag(nbtCompound2);
    nbtArray2.appendTag(new NBTTagCompound());

    NBTBase[] nbts =
        new NBTBase[] {
          nbtString, nbtByteArray, nbtLong, nbtArray1, nbtCompound1, nbtCompound2, nbtArray2
        };

    for (NBTBase nbt : nbts) {
      assertEquals(nbt, CapabilitySerializer.dataToNbt(CapabilitySerializer.nbtToData(nbt)));
    }
  }
}

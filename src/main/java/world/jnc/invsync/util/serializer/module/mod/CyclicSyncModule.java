package world.jnc.invsync.util.serializer.module.mod;

import com.lothrazar.cyclicmagic.capability.IPlayerExtendedProperties;
import com.lothrazar.cyclicmagic.playerupgrade.storage.InventoryPlayerExtended;
import com.lothrazar.cyclicmagic.registry.CapabilityRegistry;
import com.lothrazar.cyclicmagic.util.UtilPlayerInventoryFilestorage;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.serializer.NativeInventorySerializer;

public class CyclicSyncModule extends BaseModSyncModule {
  @Override
  public String getModId() {
    return "cyclicmagic";
  }

  @Override
  public DataView serialize(Player player, DataView container) {
    return Helper.serialize(player, container);
  }

  @Override
  public void deserialize(Player player, DataView container) {
    Helper.deserialize(player, container);
  }

  // Adapt an InventoryPlayerExtended into an IItemHandlerModifiable. Only the parts of the
  // interface that are used by NativeInventorySerializer are implemented.
  private static class InventoryAdapter implements IItemHandlerModifiable {
    private InventoryPlayerExtended inventory;

    private InventoryAdapter(InventoryPlayerExtended inventory) {
      this.inventory = inventory;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
      inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlots() {
      return inventory.getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
      return inventory.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
      return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
      return 0;
    }
  }

  @UtilityClass
  private static class Helper {
    private static final DataQuery CRAFTING_CAPABILITY = DataQuery.of("crafting_capability");
    private static final DataQuery INVENTORY_CAPABILITY = DataQuery.of("inventory_capability");
    private static final DataQuery INVENTORY = DataQuery.of("inventory");

    private static DataView serialize(Player player, DataView container) {
      EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);
      DataContainer serializedData = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);

      IPlayerExtendedProperties props = CapabilityRegistry.getPlayerProperties(nativePlayer);
      serializedData.set(CRAFTING_CAPABILITY, props.hasInventoryCrafting());
      serializedData.set(INVENTORY_CAPABILITY, props.hasInventoryExtended());

      InventoryPlayerExtended inventory =
          UtilPlayerInventoryFilestorage.getPlayerInventory(
              NativeInventorySerializer.getNativePlayer(player));
      serializedData.set(
          INVENTORY, NativeInventorySerializer.serializeInventory(new InventoryAdapter(inventory)));

      container.set(THIS, serializedData);
      return container;
    }

    private static void deserialize(Player player, DataView container) {
      Optional<DataView> serializedData = container.getView(THIS);
      if (getDebug()) {
        getLogger().info("\t\tisPresent:");
        getLogger().info("\t\t\tserializedData:\t" + serializedData.isPresent());
      }

      if (serializedData.isPresent()) {
        EntityPlayer nativePlayer = NativeInventorySerializer.getNativePlayer(player);
        IPlayerExtendedProperties props = CapabilityRegistry.getPlayerProperties(nativePlayer);

        Optional<Boolean> serializedCraftingCapability =
            serializedData.get().getBoolean(CRAFTING_CAPABILITY);
        Optional<Boolean> serializedInventoryCapability =
            serializedData.get().getBoolean(INVENTORY_CAPABILITY);

        boolean modifiedCapabilities = false;
        if (serializedCraftingCapability.isPresent()) {
          props.setInventoryCrafting(serializedCraftingCapability.get());
          modifiedCapabilities = true;
        }
        if (serializedInventoryCapability.isPresent()) {
          props.setInventoryExtended(serializedInventoryCapability.get());
          modifiedCapabilities = true;
        }
        if (modifiedCapabilities) {
          CapabilityRegistry.syncServerDataToClient((EntityPlayerMP) nativePlayer);
        }

        // Create a new inventory to deserialize into. This way if anything goes wrong, the current
        // extended inventory isn't overwritten.
        InventoryPlayerExtended inventory = new InventoryPlayerExtended(nativePlayer);

        Optional<List<DataView>> serializedInventory = serializedData.get().getViewList(INVENTORY);
        boolean inventoryDeserialized = false;
        if (serializedInventory.isPresent()) {
          if (NativeInventorySerializer.deserializeInventory(
              serializedInventory.get(), new InventoryAdapter(inventory))) {
            getLogger()
                .error(
                    "Could not load extended inventory of player "
                        + DataSource.getPlayerString(player)
                        + " because there were unknown item(s).");
            getLogger()
                .warn(
                    "Please make sure you are using the same mods "
                        + "on all servers you are synchronizing with.");
          } else {
            // Now assign the deserialized inventory to the player.
            UtilPlayerInventoryFilestorage.setPlayerInventory(nativePlayer, inventory);
            UtilPlayerInventoryFilestorage.syncItems(nativePlayer);
          }
        }
        getLogger()
            .info(
                "\t\t\tserializedCraftingCapability:\t" + serializedCraftingCapability.isPresent());
        getLogger()
            .info(
                "\t\t\tserializedInventoryCapability:\t"
                    + serializedInventoryCapability.isPresent());
        getLogger().info("\t\t\tserializedInventory:\t" + serializedInventory.isPresent());
      }
    }
  }
}

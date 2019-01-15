package world.jnc.invsync.permission;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.util.serializer.PlayerSerializer;

@RequiredArgsConstructor
public class PermissionRegistry {
  public static final String BASE = InventorySync.ID;
  public static final String SYNC = BASE + ".sync";

  private final InventorySync plugin;
  private final PermissionService service =
      Sponge.getServiceManager().provide(PermissionService.class).get();

  public void registerPermissions() {
    registerPermission(BASE, PermissionDescription.ROLE_ADMIN);
    registerPermission(
        SYNC, "Base permission for all synchronizing", PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.inventorySyncModule.getPermission(),
        "Allow this user's inventory to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.enderChestSyncModule.getPermission(),
        "Allow this user's ender chest to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.gameModeSyncModule.getPermission(),
        "Allow this user's game mode to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.experienceSyncModule.getPermission(),
        "Allow this user's experience to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.healthSyncModule.getPermission(),
        "Allow this user's health to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.hungerSyncModule.getPermission(),
        "Allow this user's hunger to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        PlayerSerializer.healthSyncModule.getPermission(),
        "Allow this user's potion effects to be synchronized",
        PermissionDescription.ROLE_USER);
    if (PlayerSerializer.baublesSyncModule.canBeEnabled())
      registerPermission(
          PlayerSerializer.baublesSyncModule.getPermission(),
          "Allow this user's baubles inventory to be synchronized",
          PermissionDescription.ROLE_USER);
    if (PlayerSerializer.solCarrotSyncModule.canBeEnabled())
      registerPermission(
          PlayerSerializer.solCarrotSyncModule.getPermission(),
          "Allow this user's solcarrot food list to be synchronized",
          PermissionDescription.ROLE_USER);
  }

  private Builder getBuilder() {
    return service.newDescriptionBuilder(plugin);
  }

  private void registerPermission(String permission, String role) {
    registerPermission(permission, null, role);
  }

  private void registerPermission(String permission, @Nullable String description, String role) {
    getBuilder()
        .id(permission)
        .description((description == null) ? Text.of() : Text.of(description))
        .assign(role, true)
        .register();
  }
}

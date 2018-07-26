package world.jnc.invsync.permission;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import world.jnc.invsync.InventorySync;

@RequiredArgsConstructor
public class PermissionRegistry {
  public static final String BASE = InventorySync.ID;
  public static final String SYNC = BASE + ".sync";
  public static final String SYNC_INVENTORY = SYNC + ".inventory";
  public static final String SYNC_ENDER_CHEST = SYNC + ".ender_chest";
  public static final String SYNC_GAME_MODE = SYNC + ".game_mode";
  public static final String SYNC_EXPERIENCE = SYNC + ".experience";
  public static final String SYNC_HEALTH = SYNC + ".health";
  public static final String SYNC_HUNGER = SYNC + ".hunger";
  public static final String SYNC_POTION_EFFECTS = SYNC + ".potion_effects";

  private final InventorySync plugin;
  private final PermissionService service =
      Sponge.getServiceManager().provide(PermissionService.class).get();

  public void registerPermissions() {
    registerPermission(BASE, PermissionDescription.ROLE_ADMIN);
    registerPermission(
        SYNC, "Base permission for all synchronizing", PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_INVENTORY,
        "Allow this user's inventory to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_ENDER_CHEST,
        "Allow this user's ender chest to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_GAME_MODE,
        "Allow this user's game mode to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_EXPERIENCE,
        "Allow this user's experience to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_HEALTH,
        "Allow this user's health to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_HUNGER,
        "Allow this user's hunger to be synchronized",
        PermissionDescription.ROLE_USER);
    registerPermission(
        SYNC_POTION_EFFECTS,
        "Allow this user's potion effects to be synchronized",
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

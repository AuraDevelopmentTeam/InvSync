package world.jnc.invsync.util;

import org.spongepowered.api.entity.living.player.gamemode.GameMode;

import lombok.Data;

@Data(staticConstructor = "of")
public class PlayerData {
	private final GameMode gameMode;
	private final int experience;
	private final byte[] inventory;
	private final byte[] enderChest;
}

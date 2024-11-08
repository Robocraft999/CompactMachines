package dev.compactmods.machines.room;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.Translations;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.network.SyncRoomMetadataPacket;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class RoomEventHandler {
    private static final Logger LOGS = LoggingUtil.modLog();

    /**
     * Handles an entity that has moved to a non-CM dimension, clearing their history
     * @param dimensionEvent
     */
    public static void entityChangedDimensions(final EntityTravelToDimensionEvent dimensionEvent) {
        final var ent = dimensionEvent.getEntity();
        if(!(ent instanceof Player p)) return;

        if(!CompactDimension.isLevelCompact(dimensionEvent.getDimension())) {
            if(p instanceof ServerPlayer sp) {
                LOGS.debug("Resetting player {} room history due to dimension change.", sp.getDisplayName());
                PlayerUtil.resetPlayerHistory(sp);
            }
        }
    }

    @SubscribeEvent
    public static void entityJoined(final EntityJoinLevelEvent evt) {
        Entity ent = evt.getEntity();

        // no-op clients and non-compact dimensions, we only care about server spawns
        if (!CompactDimension.isLevelCompact(ent.level()) || ent.level().isClientSide)
            return;

        // Handle players
        if (ent instanceof ServerPlayer serverPlayer) {
            RoomApi.chunkManager().findRoomByChunk(serverPlayer.chunkPosition()).ifPresent(code -> {
                CMNetworks.sendTo(new SyncRoomMetadataPacket(code, serverPlayer.getUUID()), serverPlayer);
            });
        } else {
            if (!positionInsideRoom(ent, ent.position())) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(final MobSpawnEvent evt) {
        Vec3 target = new Vec3(evt.getX(), evt.getY(), evt.getZ());

        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if (!CompactDimension.isLevelCompact(ent.level())) return;

        if (!positionInsideRoom(ent, target))
            evt.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityTeleport(final EntityTeleportEvent evt) {
        // Allow teleport commands, we don't want to trap people anywhere
        if (evt instanceof EntityTeleportEvent.TeleportCommand) return;
        if (!evt.getEntity().level().dimension().equals(CompactDimension.LEVEL_KEY)) return;

        Entity ent = evt.getEntity();
        doEntityTeleportHandle(evt, evt.getTarget(), ent);
    }


    /**
     * Helper to determine if an event should be canceled, by determining if a target is outside
     * a machine's bounds.
     *
     * @param entity Entity trying to teleport.
     * @param target Teleportation target location.
     * @return True if position is inside a room; false otherwise.
     */
    private static boolean positionInsideRoom(Entity entity, Vec3 target) {
        if (!CompactDimension.isLevelCompact(entity.level())) return false;

        return RoomApi.chunkManager()
                .findRoomByChunk(entity.chunkPosition())
                .flatMap(RoomApi::room)
                .map(ib -> ib.boundaries().innerBounds().contains(target))
                .orElse(false);
    }

    private static void doEntityTeleportHandle(EntityTeleportEvent evt, Vec3 target, Entity ent) {
        if (!positionInsideRoom(ent, target)) {
            if (ent instanceof ServerPlayer sp) {
                sp.displayClientMessage(Translations.TELEPORT_OUT_OF_BOUNDS.get(), true);
            }

            evt.setCanceled(true);
        }
    }
}

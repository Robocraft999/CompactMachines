package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
import dev.compactmods.machines.api.room.history.PlayerHistoryApi;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.dimension.SimpleTeleporter;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.network.SyncRoomMetadataPacket;
import dev.compactmods.machines.player.capability.IPlayerRoomData;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.player.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import static dev.compactmods.machines.api.room.history.RoomEntryResult.FAILED_TOO_FAR_DOWN;
import static dev.compactmods.machines.api.room.history.RoomEntryResult.SUCCESS;

public abstract class RoomHelper {

    private static final Logger LOGS = LoggingUtil.modLog();

    public static boolean entityInsideRoom(LivingEntity entity, String roomCode) {
        // Recursion check. Player is inside the room being queried.
        if (entity.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
            return RoomApi.chunks(roomCode).hasChunk(entity.chunkPosition());
        }

        return false;
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
        MinecraftServer serv = machineLevel.getServer();


        LOGS.debug("Player {} entering machine at: {}", player.getName(), machinePos);
        RoomApi.room(roomCode).ifPresent(roomInfo -> {
            try {
                teleportPlayerIntoRoom(serv, player, roomInfo, RoomEntryPoint.playerEnteringMachine(player));
            } catch (MissingDimensionException e) {
                LOGS.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        });
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, RoomInstance room, RoomEntryPoint entryPoint)
            throws MissingDimensionException {
        final var compactDim = CompactDimension.forServer(serv);

        final var history = PlayerHistoryApi.historyManager();
        final var result = history.enterRoom(player, room.code(), entryPoint);

        LOGS.debug("Entry result: {}", result);

        switch (result) {
            case FAILED_TOO_FAR_DOWN -> {
                player.displayClientMessage(Component.translatableWithFallback("compactmachines.errors.too_far_down", "An otherworldly force prevents you from shrinking more.")
                        .withStyle(ChatFormatting.DARK_RED)
                        .withStyle(ChatFormatting.ITALIC), true);
            }

            case SUCCESS -> {

                // Mark current room
                //player.setData(Rooms.DataAttachments.CURRENT_ROOM_CODE, room.code());
                //player.setData(Rooms.DataAttachments.LAST_ROOM_ENTRYPOINT, RoomEntryPoint.playerEnteringMachine(player));
                player.getCapability(Rooms.DataAttachments.PLAYER_ROOM_DATA).ifPresent(cap -> {
                    cap.setCurrentRoomCode(room.code());
                    cap.setLastRoomEntryPoint(RoomEntryPoint.playerEnteringMachine(player));
                });
                // UUID owner;
//			try {
//			   owner = RoomApi.owners().getRoomOwner(room.code());
//			} catch (NonexistentRoomException e) {
//			   LoggingUtil.modLog().debug("SCREM", e);
//			   owner = Util.NIL_UUID;
//			}

                serv.submitAsync(() -> {
                    player.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), 25);

                    final var spawns = RoomApi.spawnManager(room.code()).spawns();
                    final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
                    player.changeDimension(compactDim, SimpleTeleporter.to(spawn.position(), spawn.rotation()));

                    CMNetworks.sendTo(new SyncRoomMetadataPacket(room.code(), Util.NIL_UUID), player);
                });
            }
        }
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {
        if (!serverPlayer.level().dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        MinecraftServer serv = serverPlayer.getServer();
        assert serv != null;

        final IPlayerEntryPointHistoryManager history = PlayerHistoryApi.historyManager();

        serv.submit(() -> {
            history.lastHistory(serverPlayer).ifPresentOrElse(
                    before -> {
                        serverPlayer.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), 25);

                        serverPlayer.getCapability(Rooms.DataAttachments.PLAYER_ROOM_DATA).ifPresent(cap -> cap.setLastRoomEntryPoint(before.entryPoint()));
                        //serverPlayer.setData(Rooms.DataAttachments.LAST_ROOM_ENTRYPOINT, before.entryPoint());
                        history.popHistory(serverPlayer, 1);

                        serverPlayer.getCapability(Rooms.DataAttachments.PLAYER_ROOM_DATA).ifPresent(cap -> cap.setCurrentRoomCode(before.roomCode()));
                        //serverPlayer.setData(Rooms.DataAttachments.CURRENT_ROOM_CODE, before.roomCode());

                        final var location = before.entryPoint().entryLocation();
                        final var level = serv.getLevel(location.dimension());
                        if (level != null) {
                            LOGS.debug("Teleporting player {} to {} as they jump up a level...", serverPlayer.getUUID(), location);
                            serverPlayer.changeDimension(level, SimpleTeleporter.to(location.position(), location.rotation()));
                        } else {
                            LOGS.error("Player tracking points to an unknown dimension. Teleporting player {} to their default spawn instead.", serverPlayer.getUUID());
                            PlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                        }
                    },
                    () -> {
                        //serverPlayer.removeData(Rooms.DataAttachments.LAST_ROOM_ENTRYPOINT);
                        //serverPlayer.removeData(Rooms.DataAttachments.CURRENT_ROOM_CODE);
                        serverPlayer.getCapability(Rooms.DataAttachments.PLAYER_ROOM_DATA).ifPresent(IPlayerRoomData::remove);
                        PlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                    }
            );

        });
    }
}

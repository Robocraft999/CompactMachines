package dev.compactmods.machines.server;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachinesApi;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.IRoomRegistrar;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
import dev.compactmods.machines.api.room.history.IPlayerHistoryApi;
import dev.compactmods.machines.api.room.history.PlayerHistoryApi;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.data.manager.CMSingletonDataFileManager;
import dev.compactmods.machines.player.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.room.RoomApiInstance;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CompactMachinesApi.MOD_ID)
public class CompactMachinesServer {

    @ApiStatus.Internal // NO TOUCHY  #INeedServerCapabilities
    public static CMSingletonDataFileManager<PlayerEntryPointHistoryManager> PLAYER_HISTORY;

    private static @Nullable MinecraftServer CURRENT_SERVER;

    @SubscribeEvent
    public static void serverStarting(final ServerStartingEvent event){
        final var modLog = LoggingUtil.modLog();

        modLog.debug("Setting up room API and data");
        MinecraftServer server = event.getServer();

        PLAYER_HISTORY = new CMSingletonDataFileManager<>(server, "player_entrypoint_history", serv -> new PlayerEntryPointHistoryManager(5));

        final var registrarData = new CMSingletonDataFileManager<>(server, "room_registrations", serv -> new RoomRegistrar());
        final IRoomRegistrar registrar = registrarData.data();

        final IRoomSpawnManagers spawnManager = new RoomSpawnManagers(registrar);

        final var gcm = new GraphChunkManager();
        registrar.allRooms().forEach(inst -> gcm.calculateChunks(inst.code(), inst.boundaries()));

        RoomApi.INSTANCE = new RoomApiInstance(registrar::isRegistered, registrar, spawnManager, gcm);

        PlayerHistoryApi.INSTANCE = () -> PLAYER_HISTORY.data();

        modLog.debug("Completed setting up room API and data.");
    }

    public static void save() {
        if(CURRENT_SERVER != null) {
            PLAYER_HISTORY.save();
            //ROOM_DATA_ATTACHMENTS.save(CURRENT_SERVER.registryAccess());
        }
    }

    @SubscribeEvent
    public static void serverStopping(final ServerStoppingEvent evt) {
        save();
    }

    @SubscribeEvent
    public static void levelSaved(final LevelEvent.Save level) {
        if (level.getLevel() instanceof Level l && CompactDimension.isLevelCompact(l)) {
            save();
        }
    }
}

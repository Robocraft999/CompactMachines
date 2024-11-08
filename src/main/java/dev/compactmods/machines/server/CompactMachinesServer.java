package dev.compactmods.machines.server;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachinesApi;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.IRoomRegistrar;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.history.PlayerHistoryApi;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.data.DataFileUtil;
import dev.compactmods.machines.data.manager.CMSingletonDataFileManager;
import dev.compactmods.machines.player.PlayerEntryPointHistoryManager;
import dev.compactmods.machines.room.RoomApiInstance;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = CompactMachinesApi.MOD_ID)
public class CompactMachinesServer {

    private static @Nullable MinecraftServer CURRENT_SERVER;
    private static RoomRegistrar ROOM_REGISTRAR;
    private static PlayerEntryPointHistoryManager PLAYER_HISTORY;

    private static CMSingletonDataFileManager<RoomRegistrar> ROOM_REGISTRAR_DATA;
    private static CMSingletonDataFileManager<PlayerEntryPointHistoryManager> PLAYER_HISTORY_DATA;

    @SubscribeEvent
    public static void serverStarting(final ServerStartingEvent event){
        final var modLog = LoggingUtil.modLog();

        modLog.debug("Setting up room API and data");
        MinecraftServer server = event.getServer();

        if (CompactMachinesServer.CURRENT_SERVER != null) {
            save();
        }

        PLAYER_HISTORY = new PlayerEntryPointHistoryManager(5);
        PLAYER_HISTORY_DATA = new CMSingletonDataFileManager<>(server, "player_entrypoint_history", PLAYER_HISTORY);

        var file = RoomRegistrar.getFile(server);
        ROOM_REGISTRAR = file.exists() ? DataFileUtil.loadFileWithCodec(file, RoomRegistrar.CODEC) : new RoomRegistrar();
        ROOM_REGISTRAR_DATA = new CMSingletonDataFileManager<>(server, "room_registrations", ROOM_REGISTRAR);

        final IRoomSpawnManagers spawnManager = new RoomSpawnManagers(ROOM_REGISTRAR);

        final var gcm = new GraphChunkManager();
        ROOM_REGISTRAR.allRooms().forEach(inst -> gcm.calculateChunks(inst.code(), inst.boundaries()));

        RoomApi.INSTANCE = new RoomApiInstance(ROOM_REGISTRAR::isRegistered, ROOM_REGISTRAR, spawnManager, gcm);

        PlayerHistoryApi.INSTANCE = () -> PLAYER_HISTORY_DATA.data();

        CURRENT_SERVER = server;

        modLog.debug("Completed setting up room API and data.");
    }

    public static void save() {
        if(CURRENT_SERVER != null) {
            ROOM_REGISTRAR_DATA.save();
            PLAYER_HISTORY_DATA.save();
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

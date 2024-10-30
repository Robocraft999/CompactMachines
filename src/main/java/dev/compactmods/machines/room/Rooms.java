package dev.compactmods.machines.room;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.player.capability.IPlayerRoomData;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.ui.preview.MachineRoomMenu;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.MathUtil;
import dev.compactmods.machines.wall.BreakableWallBlock;
import dev.compactmods.machines.wall.ItemBlockWall;
import dev.compactmods.machines.wall.SolidWallBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import javax.naming.OperationNotSupportedException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Rooms {

    interface Blocks {
        RegistryObject<SolidWallBlock> SOLID_WALL = Registries.BLOCKS.register("solid_wall", () ->
                new SolidWallBlock(BlockBehaviour.Properties.of()
                        .strength(-1.0F, 3600000.8F)
                        .sound(SoundType.METAL)
                        .lightLevel((state) -> 15)));


        RegistryObject<BreakableWallBlock> BREAKABLE_WALL = Registries.BLOCKS.register("wall", () ->
                new BreakableWallBlock(BlockBehaviour.Properties.of()
                        .strength(3.0f, 128.0f)
                        .requiresCorrectToolForDrops()));

        static void prepare() {
        }
    }

    interface Items {
        Supplier<Item.Properties> WALL_ITEM_PROPS = Item.Properties::new;

        RegistryObject<ItemBlockWall> ITEM_SOLID_WALL = Registries.ITEMS.register("solid_wall", () ->
                new ItemBlockWall(Blocks.SOLID_WALL.get(), WALL_ITEM_PROPS.get()));

        RegistryObject<ItemBlockWall> BREAKABLE_WALL = Registries.ITEMS.register("wall", () ->
                new ItemBlockWall(Blocks.BREAKABLE_WALL.get(), WALL_ITEM_PROPS.get()));

        static void prepare() {
        }
    }

    interface Menus {
        RegistryObject<MenuType<MachineRoomMenu>> MACHINE_MENU = Registries.CONTAINERS.register("machine", () ->
                IForgeMenuType.create(MachineRoomMenu::createClientMenu));
        static void prepare() {
        }
    }

    interface DataAttachments {
        Capability<IPlayerRoomData> PLAYER_ROOM_DATA = CapabilityManager.get(new CapabilityToken<>(){});

        static void prepare() {
        }
    }

    static void prepare() {
        Blocks.prepare();
        Items.prepare();
        Menus.prepare();
        DataAttachments.prepare();
    }

    public static ChunkPos createNew(MinecraftServer serv, RoomSize size, UUID owner) throws MissingDimensionException {
        final var compactWorld = CompactDimension.forServer(serv);
        if (compactWorld == null)
            throw new MissingDimensionException();

        CompactRoomData rooms = CompactRoomData.get(compactWorld);

        int nextPosition = rooms.getNextSpiralPosition();
        Vec3i location = MathUtil.getRegionPositionByIndex(nextPosition);

        BlockPos newCenter = BlockPos.containing(MathUtil.getCenterWithY(location, ServerConfig.MACHINE_FLOOR_Y.get()));

        // Generate a new machine room
        CompactStructureGenerator.generateCompactStructure(compactWorld, size.toVec3(), newCenter);

        ChunkPos machineChunk = new ChunkPos(newCenter);
        try {
            rooms.createNew()
                    .owner(owner)
                    .size(size)
                    .chunk(machineChunk)
                    .register();
        } catch (OperationNotSupportedException e) {
            // room already registered somehow
            CompactMachines.LOGGER.warn(e);
        }

        return machineChunk;
    }

    public static RoomSize sizeOf(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim)
                .getData(room)
                .getSize();
    }

    public static IDimensionalPosition getSpawn(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim).getSpawn(room);
    }

    public static boolean exists(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim).isRegistered(room);
    }

    public static StructureTemplate getInternalBlocks(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        final var tem = new StructureTemplate();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var bounds = roomInfo.getRoomBounds();
        final int inside = roomInfo.getSize().getInternalSize();
        tem.fillFromWorld(compactDim, new BlockPos((int)bounds.minX, (int)bounds.minY - 1, (int)bounds.minZ),
                new Vec3i(inside, inside + 1, inside), false, null);

        return tem;
    }

    static CompletableFuture<StructureTemplate> getInternalBlocks(MinecraftServer server, String roomCode) {
        final var template = new StructureTemplate();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var chunkSource = compactDim.getChunkSource();

        final var chunkLoading = RoomApi.chunks(roomCode)
                .stream()
                .map(cp -> chunkSource.getChunkFuture(cp.x, cp.z, ChunkStatus.FULL, true))
                .toList();

        final var awaitAllChunks = CompletableFuture.allOf(chunkLoading.toArray(new CompletableFuture[chunkLoading.size()]));
        return awaitAllChunks.thenApply(ignored -> {
            RoomApi.room(roomCode).ifPresentOrElse(inst -> {
                    var bounds = inst.boundaries().innerBounds();
                    template.fillFromWorld(
                            compactDim,
                            BlockPos.containing(bounds.minX, bounds.minY -1, bounds.minZ),
                            new Vec3i((int)bounds.getXsize(), (int)bounds.getYsize() + 1, (int)bounds.getZsize()),
                            false,
                            null);
                    LoggingUtil.modLog().info(template.getSize());
                },
                () -> {
                    //TODO throw error
                    LoggingUtil.modLog().error("Could not get Internal Blocks of room: " + roomCode);
                }
            );
            return template;
        });
    }

    public static void resetSpawn(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var centerPoint = Vec3.atCenterOf(roomInfo.getCenter());
        final var newSpawn = centerPoint.subtract(0, (roomInfo.getSize().getInternalSize() / 2f), 0);

        data.setSpawn(room, newSpawn, Vec2.ZERO);
    }

    public static Optional<String> getRoomName(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        return roomInfo.getName();
    }

    public static Optional<GameProfile> getOwner(MinecraftServer server, ChunkPos room) {
        if (!exists(server, room))
            return Optional.empty();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var data = CompactRoomData.get(compactDim);

        try {
            final CompactRoomData.RoomData roomInfo = data.getData(room);
            final var ownerUUID = roomInfo.getOwner();

            return server.getProfileCache().get(ownerUUID);
        } catch (NonexistentRoomException e) {
            return Optional.empty();
        }
    }

    public static void updateName(MinecraftServer server, ChunkPos room, String newName) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        roomInfo.setName(newName);
        data.setDirty();
    }
}

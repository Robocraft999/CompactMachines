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

    public static boolean exists(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim).isRegistered(room);
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
}

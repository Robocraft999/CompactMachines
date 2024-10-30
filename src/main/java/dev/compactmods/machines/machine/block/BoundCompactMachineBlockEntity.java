package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.machine.block.IBoundCompactMachineBlockEntity;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.capability.IMachineColorCapability;
import dev.compactmods.machines.machine.capability.MachineColorCapabilityImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BoundCompactMachineBlockEntity extends BlockEntity implements IBoundCompactMachineBlockEntity {
    protected UUID owner;
    private String roomCode;
    @Nullable
    private Component customName;
    private LazyOptional<IMachineColorCapability> capability;

    public BoundCompactMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Machines.BlockEntities.MACHINE.get(), pPos, pBlockState);
        capability = LazyOptional.of(MachineColorCapabilityImpl::getDefault);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains(NBT_ROOM_CODE)){
            this.roomCode = pTag.getString(NBT_ROOM_CODE);
        }

        if (pTag.contains(NBT_OWNER)) {
            owner = pTag.getUUID(NBT_OWNER);
        } else {
            owner = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (owner != null) {
            pTag.putUUID(NBT_OWNER, this.owner);
        }

        if (roomCode != null)
            pTag.putString(NBT_ROOM_CODE, roomCode);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var data = super.getUpdateTag();

        if (this.roomCode != null)
            data.putString(NBT_ROOM_CODE, this.roomCode);

        if (this.owner != null)
            data.putUUID(NBT_OWNER, this.owner);

        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if(tag.contains(NBT_ROOM_CODE))
            this.roomCode = tag.getString(NBT_ROOM_CODE);

        if (tag.contains("players")) {
            CompoundTag players = tag.getCompound("players");
            // playerData = CompactMachinePlayerData.fromNBT(players);
        }

        if (tag.contains(NBT_OWNER))
            owner = tag.getUUID(NBT_OWNER);
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean hasPlayersInside() {
        // TODO
        return false;
    }

    public GlobalPos getLevelPosition() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public void setConnectedRoom(String roomCode) {
        if (level instanceof ServerLevel sl) {
            // FIXME: Register machine location in room's connection graph
//            final var dimMachines = DimensionMachineGraph.forDimension(sl);
//            if (this.roomCode != null) {
//                dimMachines.unregisterMachine(worldPosition);
//            }
//
//            dimMachines.register(worldPosition, roomCode);
            this.roomCode = roomCode;

            RoomApi.room(roomCode).ifPresentOrElse(inst -> {
                    getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(inst.defaultMachineColor()));
                    //this.setData(Machines.Attachments.MACHINE_COLOR, inst.defaultMachineColor());
                },
                () -> {
                    getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(DyeColor.WHITE.getTextColor()));
                    //this.setData(Machines.Attachments.MACHINE_COLOR, DyeColor.WHITE.getTextColor());
                }
            );

            this.setChanged();
        }
    }

    public void disconnect() {
        if (level instanceof ServerLevel sl) {
            // FIXME: Room machine graph unregister
//            final var dimMachines = DimensionMachineGraph.forDimension(sl);
//            dimMachines.unregisterMachine(worldPosition);

            sl.setBlock(worldPosition, Machines.Blocks.UNBOUND_MACHINE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @NotNull
    public String connectedRoom() {
        return roomCode;
    }

    public Optional<Component> getCustomName() {
        return Optional.ofNullable(customName);
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        this.setChanged();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == Machines.Attachments.MACHINE_COLOR){
            return capability.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capability.invalidate();
    }
}

package dev.compactmods.machines.player.capability;

import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRoomDataImpl {
    public static IPlayerRoomData getDefault() {
        return new DefaultImpl();
    }

    public static class DefaultImpl implements IPlayerRoomData{
        private RoomEntryPoint entryPoint;
        private String currentRoomCode;
        private GlobalPos openMachinePos;


        @Override
        public RoomEntryPoint getLastRoomEntryPoint() {
            return entryPoint;
        }

        @Override
        public void setLastRoomEntryPoint(RoomEntryPoint entryPoint) {
            this.entryPoint = entryPoint;
        }

        @Override
        public String getCurrentRoomCode() {
            return currentRoomCode;
        }

        @Override
        public void setCurrentRoomCode(String code) {
            this.currentRoomCode = code;
        }

        @Override
        public GlobalPos getOpenMachinePos() {
            return openMachinePos;
        }

        @Override
        public void setOpenMachinePos(GlobalPos globalPos) {
            this.openMachinePos = globalPos;
        }

        @Override
        public void remove() {

        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("openMachinePos", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, this.openMachinePos).result().orElseThrow());
            tag.putString("currentRoomCode", this.currentRoomCode);
            tag.put("lastRoomEntrypoint", RoomEntryPoint.CODEC.encodeStart(NbtOps.INSTANCE, this.entryPoint).result().orElseThrow());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("openMachinePos")).result().ifPresent(pos -> this.openMachinePos = pos.getFirst());
            this.currentRoomCode = nbt.getString("currentRoomCode");
            RoomEntryPoint.CODEC.decode(NbtOps.INSTANCE, nbt.get("lastRoomEntrypoint")).result().ifPresentOrElse(entry -> this.entryPoint = entry.getFirst(), () -> this.entryPoint = RoomEntryPoint.INVALID);
        }
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        private final NonNullSupplier<IPlayerRoomData> supplier;
        private LazyOptional<IPlayerRoomData> cachedCapability;

        public Provider(){
            IPlayerRoomData cap = new DefaultImpl();
            supplier = () -> cap;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if (capability == Machines.Attachments.MACHINE_COLOR){
                if (cachedCapability == null || !cachedCapability.isPresent()){
                    cachedCapability = LazyOptional.of(supplier);
                }
                return cachedCapability.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return supplier.get().serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag compoundTag) {
            supplier.get().deserializeNBT(compoundTag);
        }
    }
}

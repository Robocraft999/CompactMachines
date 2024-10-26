package dev.compactmods.machines.machine.capability;

import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.CommonColors;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineColorCapabilityImpl {
    public static IMachineColorCapability getDefault() {
        return new DefaultImpl();
    }

    public static class DefaultImpl implements IMachineColorCapability{
        private int color = CommonColors.WHITE;
        private static final String NBT_COLOR = "machine_color";

        @Override
        public int getColor() {
            return color;
        }

        @Override
        public void setColor(int color) {
            this.color = color;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt(NBT_COLOR, color);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag compoundTag) {
            this.color = compoundTag.getInt(NBT_COLOR);
        }
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag>{
        private final NonNullSupplier<IMachineColorCapability> supplier;
        private LazyOptional<IMachineColorCapability> cachedCapability;

        public Provider(){
            IMachineColorCapability cap = new DefaultImpl();
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

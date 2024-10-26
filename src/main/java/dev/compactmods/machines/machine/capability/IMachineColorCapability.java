package dev.compactmods.machines.machine.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMachineColorCapability extends INBTSerializable<CompoundTag> {
    int getColor();
    void setColor(int color);
}

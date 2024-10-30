package dev.compactmods.machines.player.capability;

import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPlayerRoomData extends INBTSerializable<CompoundTag> {
    RoomEntryPoint getLastRoomEntryPoint();
    void setLastRoomEntryPoint(RoomEntryPoint entryPoint);

    String getCurrentRoomCode();
    void setCurrentRoomCode(String code);

    GlobalPos getOpenMachinePos();
    void setOpenMachinePos(GlobalPos globalPos);

    void remove();
}

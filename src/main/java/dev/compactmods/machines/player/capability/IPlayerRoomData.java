package dev.compactmods.machines.player.capability;

import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IPlayerRoomData extends INBTSerializable<CompoundTag> {
    RoomEntryPoint getLastRoomEntryPoint();
    void setLastRoomEntryPoint(RoomEntryPoint entryPoint);

    String getCurrentRoomCode();
    void setCurrentRoomCode(String code);

    void remove();
}

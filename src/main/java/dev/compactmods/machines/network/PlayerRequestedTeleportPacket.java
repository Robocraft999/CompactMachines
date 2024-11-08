package dev.compactmods.machines.network;

import dev.compactmods.machines.room.RoomHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String roomCode) implements ICMPacket{
    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
           final var player = context.getSender();
           if (player != null){
               RoomHelper.teleportPlayerIntoMachine(player.level(), player, machine, roomCode);
           }
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeGlobalPos(machine);
        buffer.writeUtf(roomCode);
    }

    public static PlayerRequestedTeleportPacket decode(FriendlyByteBuf buf) {
        var machine = buf.readGlobalPos();
        var roomCode = buf.readUtf();
        return new PlayerRequestedTeleportPacket(machine, roomCode);
    }
}

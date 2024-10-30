package dev.compactmods.machines.network;

import dev.compactmods.machines.room.Rooms;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record PlayerStartedRoomTrackingPacket(String roomCode) implements ICMPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        var serverPlayer = context.getSender();
        if (serverPlayer != null){
            var blocks = Rooms.getInternalBlocks(serverPlayer.server, roomCode);
            CMNetworks.sendTo(new InitialRoomBlockDataPacket(blocks), serverPlayer);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(roomCode);
    }

    public static PlayerStartedRoomTrackingPacket decode(FriendlyByteBuf buffer){
        return new PlayerStartedRoomTrackingPacket(buffer.readUtf());
    }
}

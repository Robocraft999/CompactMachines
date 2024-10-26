package dev.compactmods.machines.network;

import dev.compactmods.machines.client.room.ClientRoomPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public record SyncRoomMetadataPacket(String roomCode, UUID owner) implements ICMPacket {

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientRoomPacketHandler.handleRoomSync(roomCode, owner);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {

    }
}

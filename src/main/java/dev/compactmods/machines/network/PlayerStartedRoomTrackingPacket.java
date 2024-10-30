package dev.compactmods.machines.network;

import dev.compactmods.machines.room.Rooms;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public record PlayerStartedRoomTrackingPacket(String roomCode) implements ICMPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        var serverPlayer = context.getSender();
        context.enqueueWork(() -> {
            StructureTemplate blocks;
            if (serverPlayer == null)
                return;
            try{
                blocks = Rooms.getInternalBlocks(serverPlayer.server, roomCode).get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            CMNetworks.sendTo(new InitialRoomBlockDataPacket(blocks), serverPlayer);
        });
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(roomCode);
    }

    public static PlayerStartedRoomTrackingPacket decode(FriendlyByteBuf buffer){
        return new PlayerStartedRoomTrackingPacket(buffer.readUtf());
    }
}

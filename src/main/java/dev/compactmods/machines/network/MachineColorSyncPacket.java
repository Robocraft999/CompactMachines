package dev.compactmods.machines.network;

import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.client.ClientEventHandler;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record MachineColorSyncPacket(GlobalPos position, MachineColor color) implements ICMPacket{
    @Override
    public void handle(NetworkEvent.Context context) {
        ClientEventHandler.setMachineColor(position, color);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeGlobalPos(position);
        buffer.writeJsonWithCodec(MachineColor.CODEC, color);
    }

    public static MachineColorSyncPacket decode(FriendlyByteBuf buffer){
        return new MachineColorSyncPacket(buffer.readGlobalPos(), buffer.readJsonWithCodec(MachineColor.CODEC));
    }
}

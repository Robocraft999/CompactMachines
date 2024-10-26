package dev.compactmods.machines.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface ICMPacket {
    void handle(NetworkEvent.Context context);

    void encode(FriendlyByteBuf buffer);

    static <PACKET extends ICMPacket> void handle(final PACKET message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> message.handle(context));
        context.setPacketHandled(true);
    }
}

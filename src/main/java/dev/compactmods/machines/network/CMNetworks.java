package dev.compactmods.machines.network;

import dev.compactmods.machines.api.CompactMachinesApi;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

public class CMNetworks {
    private static final String PROTOCOL_VERSION = "6.0.0";
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CompactMachinesApi.MOD_ID, "main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int index;

    private static <MSG extends ICMPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG extends ICMPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <MSG extends ICMPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
        HANDLER.registerMessage(index++, type, ICMPacket::encode, decoder, ICMPacket::handle, Optional.of(networkDirection));
    }

    private static boolean isLocal(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.getGameProfile());
    }

    /**
     * Send a packet to a specific player.<br> Must be called Server side.
     */
    public static <MSG extends ICMPacket> void sendTo(MSG msg, ServerPlayer player) {
        if (!(player instanceof FakePlayer)) {
            HANDLER.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }

    public static <MSG extends ICMPacket> void sendToNear(MSG msg, ServerPlayer player) {
        if (player.level() != null){
            HANDLER.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(player.getX(), player.getY(), player.getZ(), 25, player.level().dimension())), msg);
        }
    }

    public static <MSG extends ICMPacket> void sendToNear(MSG msg, BlockPos pos, Level level) {
        if (level != null){
            HANDLER.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), 25, level.dimension())), msg);
        }
    }


    public static <MSG extends ICMPacket> void sendToAll(MSG msg) {
        HANDLER.send(PacketDistributor.ALL.noArg(), msg);
    }



    /**
     * Send a packet to the server.
     */
    public static <MSG extends ICMPacket> void sendToServer(MSG msg) {
        HANDLER.sendToServer(msg);
    }
}

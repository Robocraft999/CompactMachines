package dev.compactmods.machines.network;

import dev.compactmods.machines.client.room.ClientRoomPacketHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.network.NetworkEvent;

public record InitialRoomBlockDataPacket(StructureTemplate blocks) implements ICMPacket{
    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> ClientRoomPacketHandler.handleBlockData(blocks));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(blocks.save(new CompoundTag()));
    }

    public static InitialRoomBlockDataPacket decode(FriendlyByteBuf buffer){
        var template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK.asLookup(), buffer.readNbt());
        return new InitialRoomBlockDataPacket(template);
    }
}

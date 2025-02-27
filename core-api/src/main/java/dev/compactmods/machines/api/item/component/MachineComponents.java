package dev.compactmods.machines.api.item.component;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.machine.MachineColor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public interface MachineComponents {

    /**
     * Only on bound room items - given by a crafting process or when a bound machine block is broken
     */
    UnaryOperator<DataComponentType.Builder<String>> BOUND_ROOM_CODE = (builder) -> builder
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8);

    /**
     * Only on new room items - IUnboundMachineItem
     */
    UnaryOperator<DataComponentType.Builder<ResourceLocation>> ROOM_TEMPLATE_ID = (builder) -> builder
            .persistent(ResourceLocation.CODEC)
            .networkSynchronized(ResourceLocation.STREAM_CODEC);

    UnaryOperator<DataComponentType.Builder<MachineColor>> MACHINE_COLOR = (builder) -> builder
            .persistent(MachineColor.CODEC)
            .networkSynchronized(MachineColor.STREAM_CODEC);

}

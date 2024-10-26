package dev.compactmods.machines.api.machine;

import dev.compactmods.machines.api.CompactMachinesApi;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public interface MachineTranslations {

    Function<BlockPos, Component> NOT_A_MACHINE_BLOCK = (pos) -> Component.empty();

    Function<String, Component> SIZE = (roomSize) -> Component.translatableWithFallback(MachineTranslations.IDs.SIZE, "Size: %s", roomSize)
            .withStyle(ChatFormatting.YELLOW);

    interface IDs {
        String OWNER = Util.makeDescriptionId("machine", CompactMachinesApi.modRL("machine.owner"));
        String SIZE = Util.makeDescriptionId("machine", CompactMachinesApi.modRL("machine.size"));
        String BOUND_TO = Util.makeDescriptionId("machine", CompactMachinesApi.modRL("machine.bound_to"));
        String NEW_MACHINE = Util.makeDescriptionId("machine", CompactMachinesApi.modRL("new_machine"));
    }
}

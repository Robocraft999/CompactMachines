package dev.compactmods.machines.api.command;

import dev.compactmods.machines.api.CompactMachinesApi;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;
import java.util.function.Supplier;

public interface CommandTranslations {

    Supplier<Component> CANNOT_GIVE_MACHINE = () -> Component.translatableWithFallback(IDs.CANNOT_GIVE_MACHINE, "Cannot give machine.");

    Function<Player, Component> MACHINE_GIVEN = (player) -> Component
            .translatableWithFallback(IDs.MACHINE_GIVEN, "Created a new machine item and gave it to %s.", player.getDisplayName());

    interface IDs {
        String CANNOT_GIVE_MACHINE = Util.makeDescriptionId("commands.machines", CompactMachinesApi.modRL("cannot_give_machine_item"));

        String MACHINE_GIVEN = Util.makeDescriptionId("commands.machines", CompactMachinesApi.modRL("machine_given_successfully"));

        /**
         * Used for displaying the number of registered rooms via summary commands.
         */
        String ROOM_COUNT = Util.makeDescriptionId("commands.rooms", CompactMachinesApi.modRL("room_reg_count"));

        String SPAWN_CHANGED_SUCCESSFULLY = Util.makeDescriptionId("commands.rooms", CompactMachinesApi.modRL("spawn_changed_successfully"));
    }
}

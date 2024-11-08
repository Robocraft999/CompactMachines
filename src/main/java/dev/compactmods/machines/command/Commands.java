package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.CompactMachinesApi;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.command.data.CMDataSubcommand;
import dev.compactmods.machines.command.subcommand.*;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.upgrade.command.CMUpgradeRoomCommand;
import dev.compactmods.machines.upgrade.command.RoomUpgradeArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class Commands {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    static final LiteralArgumentBuilder<CommandSourceStack> CM_COMMAND_ROOT
            = LiteralArgumentBuilder.literal(CompactMachinesApi.MOD_ID);

    public static void prepare(){}

    public static LiteralArgumentBuilder<CommandSourceStack> getRoot(){
        return CM_COMMAND_ROOT;
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        Commands.CM_COMMAND_ROOT.then(CMTeleportSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMEjectSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMRebindSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMUnbindSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMRoomsSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMGiveMachineSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(SpawnSubcommand.make());

        CM_COMMAND_ROOT.then(net.minecraft.commands.Commands.literal("test").executes(ctx -> {
            final var player = ctx.getSource().getPlayerOrException();

            final var axe = player.getMainHandItem();

            if(!axe.is(ItemTags.AXES))
                return -1;

            return 0;
        }));

        event.getDispatcher().register(Commands.CM_COMMAND_ROOT);
    }
}

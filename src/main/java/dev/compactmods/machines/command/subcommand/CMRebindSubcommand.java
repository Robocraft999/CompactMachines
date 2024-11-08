package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineTranslations;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class CMRebindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("rebind")
                .requires(cs -> cs.hasPermission(ServerConfig.rebindLevel()));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("bindTo", StringArgumentType.string())
                        .executes(CMRebindSubcommand::doRebind)));

        return subRoot;
    }

    private static int doRebind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var level = ctx.getSource().getLevel();
        final var source = ctx.getSource();

        final var LOGS = LoggingUtil.modLog();

        final var roomProvider = RoomApi.registrar();
        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomCode = StringArgumentType.getString(ctx, "bindTo");

        if (!(level.getBlockEntity(rebindingMachine) instanceof BoundCompactMachineBlockEntity machine)) {
            LOGS.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            source.sendFailure(MachineTranslations.NOT_A_MACHINE_BLOCK.apply(rebindingMachine));
            return -1;
        }

        roomProvider.get(roomCode).ifPresentOrElse(targetRoom -> {
            LOGS.info("Binding machine at {} to room {}", rebindingMachine, roomCode);
            machine.setConnectedRoom(roomCode);
        }, () -> {
            LOGS.error("Cannot rebind to room {}; not registered.", roomCode);
        });

        return 0;
    }
}

package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.command.CommandTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.stream.LongStream;

public class CMSummarySubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("summary")
                .executes(CMSummarySubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

        final var ls = LongStream.builder();

        // FIXME: Per-dimension machine count
//        serv.getAllLevels().forEach(sl -> {
//            final var machineData = DimensionMachineGraph.forDimension(sl);
//            long numRegistered = machineData.machines().count();
//
//            if(numRegistered > 0) {
//                src.sendSuccess(() -> TranslationUtil.command(CMCommands.MACHINE_REG_DIM, sl.dimension().location().toString(), numRegistered), false);
//                ls.add(numRegistered);
//            }
//        });

//        long grandTotal = ls.build().sum();
//        src.sendSuccess(() -> Component.translatable(CommandTranslations.IDs.MACHINE_REG_TOTAL, grandTotal).withStyle(ChatFormatting.GOLD), false);

        final var roomCount = RoomApi.registrar().count();
        src.sendSuccess(() -> Component.translatable(CommandTranslations.IDs.ROOM_COUNT, roomCount), false);

        return 0;
    }
}

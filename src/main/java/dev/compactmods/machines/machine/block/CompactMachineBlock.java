package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.block.ICompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.network.MachineColorSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class CompactMachineBlock extends Block {
    public CompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);


        final var be = pLevel.getBlockEntity(pPos);
        if (be != null){
            AtomicInteger color = new AtomicInteger(DyeColor.WHITE.getFireworkColor());
            var tag = pStack.getOrCreateTag();
            if (tag.contains(Machines.DataComponents.MACHINE_COLOR))
                color.set(tag.getInt(Machines.DataComponents.MACHINE_COLOR));
            be.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(color.get()));
        }

        /*final var color = pStack.getOrDefault(Machines.DataComponents.MACHINE_COLOR, DyeColor.WHITE.getFireworkColor());
        final var be = level.getBlockEntity(pPos);
        if(be != null)
            be.setData(Machines.Attachments.MACHINE_COLOR, color);*/
    }

    @NotNull
    protected static InteractionResult tryDyingMachine(ServerLevel level, @NotNull BlockPos pos, Player player, DyeItem dye, ItemStack mainItem) {
        var color = dye.getDyeColor();
        final var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICompactMachineBlockEntity) {
            //blockEntity.setData(Machines.Attachments.MACHINE_COLOR, color.getFireworkColor());
            blockEntity.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(color.getFireworkColor()));

            /*PacketDistributor.sendToPlayersTrackingChunk(
                    level, new ChunkPos(pos), new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), color.getFireworkColor()));*/
            ChunkPos chunkPos = new ChunkPos(pos);
            CMNetworks.sendToTrackingChunk(new MachineColorSyncPacket(GlobalPos.of(level.dimension(), pos), MachineColor.fromARGB(color.getFireworkColor())), level, chunkPos);

            if (!player.isCreative())
                mainItem.shrink(1);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}

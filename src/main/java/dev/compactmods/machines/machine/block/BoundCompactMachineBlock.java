package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class BoundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public BoundCompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        try {
            if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
                AtomicInteger color = new AtomicInteger(CommonColors.WHITE);
                be.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> {
                    color.set(cap.getColor());
                });
                return Machines.Items.boundToRoom(be.connectedRoom(), color.get()/*be.getData(Machines.Attachments.MACHINE_COLOR)*/);
            }

            return Machines.Items.unbound();
        }

        catch(Exception ex) {
            LoggingUtil.modLog().warn("Warning: tried to pick block on a bound machine that does not have a room bound.", ex);
            return Machines.Items.unbound();
        }
    }

    @Override
    public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
        return super.getDestroyProgress(pState, pPlayer, pLevel, pPos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BoundCompactMachineBlockEntity(blockPos, blockState);
    }
}

package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.ui.preview.MachineRoomMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
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

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack mainItem = pPlayer.getItemInHand(pHand);
        //With Item
        if (!mainItem.isEmpty()) {
            if (mainItem.getItem() instanceof DyeItem dye && !pLevel.isClientSide && pLevel instanceof ServerLevel serverLevel){
                return tryDyingMachine(serverLevel, pPos, pPlayer, dye, mainItem);
            }

            if (mainItem.is(PSDTags.ITEM) && pPlayer instanceof ServerPlayer serverPlayer && pLevel.getBlockEntity(pPos) instanceof BoundCompactMachineBlockEntity entity){
                // Try to teleport player into room
                RoomHelper.teleportPlayerIntoMachine(pLevel, serverPlayer, entity.getLevelPosition(), entity.connectedRoom());
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        } else {
            //Without Item
            if (!pLevel.isClientSide && !(pPlayer instanceof FakePlayer)) {
                pLevel.getBlockEntity(pPos, Machines.BlockEntities.MACHINE.get()).ifPresent(machine -> {
                    final var roomCode = machine.connectedRoom();
                    RoomApi.room(roomCode).ifPresent(inst -> {
                        if (pPlayer instanceof ServerPlayer serverPlayer) {
                            serverPlayer.getCapability(Rooms.DataAttachments.PLAYER_ROOM_DATA).ifPresent(cap -> cap.setOpenMachinePos(machine.getLevelPosition()));
                            NetworkHooks.openScreen(serverPlayer, MachineRoomMenu.provider(serverPlayer.server, inst), (buf) -> {
                                buf.writeJsonWithCodec(GlobalPos.CODEC, machine.getLevelPosition());
                                buf.writeUtf(roomCode);
                                buf.writeOptional(Optional.<String>empty(), FriendlyByteBuf::writeUtf);
                            });
                        }
                    });
                });
            }
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }
}

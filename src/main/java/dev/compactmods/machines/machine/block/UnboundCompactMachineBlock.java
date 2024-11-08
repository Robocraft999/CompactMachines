package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.RoomHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class UnboundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public UnboundCompactMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof UnboundCompactMachineBlockEntity be) {
            final var id = be.templateId();
            final var temp = be.template().orElse(RoomTemplate.INVALID_TEMPLATE);

            if (id != null && !temp.equals(RoomTemplate.INVALID_TEMPLATE)) {
                var item = Machines.Items.forNewRoom(id, temp);
                /*be.getExistingData(Machines.Attachments.MACHINE_COLOR).ifPresent(color -> {
                    item.set(Machines.DataComponents.MACHINE_COLOR, color);
                });*/
                be.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> {
                    item.getOrCreateTag().putInt(Machines.DataComponents.MACHINE_COLOR, cap.getColor());
                });

                return item;
            }
        }

        return Machines.Items.unbound();
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);

        final var be = pLevel.getBlockEntity(pPos);
        if (be instanceof UnboundCompactMachineBlockEntity blockEntity){
            var templateId = pStack.getOrCreateTag().getString(Machines.DataComponents.ROOM_TEMPLATE_ID);
            blockEntity.setTemplate(new ResourceLocation(templateId));
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new UnboundCompactMachineBlockEntity(blockPos, blockState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack stack = player.getItemInHand(pHand);
        if (stack.getItem() instanceof DyeItem dye && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            return tryDyingMachine(serverLevel, pos, player, dye, stack);
        }

        MinecraftServer server = level.getServer();
        if (stack.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            level.getBlockEntity(pos, Machines.BlockEntities.UNBOUND_MACHINE.get()).ifPresent(unboundEntity -> {
                //RoomTemplate template = unboundEntity.template().orElse(RoomTemplate.INVALID_TEMPLATE);
                RoomTemplate template = RoomTemplateHelper.getTemplate(level, unboundEntity.templateId());
                if (!template.equals(RoomTemplate.INVALID_TEMPLATE)) {
                    AtomicInteger color = new AtomicInteger();
                    unboundEntity.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> color.set(cap.getColor()));
                    //int color = unboundEntity.getData(Machines.Attachments.MACHINE_COLOR);

                    try {
                        // Generate a new machine room
                        final var newRoom = RoomApi.newRoom(server, template, sp.getUUID());

                        // Change into a bound machine block
                        level.setBlock(pos, Machines.Blocks.BOUND_MACHINE.get().defaultBlockState(), Block.UPDATE_ALL);

                        // Set up binding and enter
                        level.getBlockEntity(pos, Machines.BlockEntities.MACHINE.get()).ifPresent(ent -> {
                            ent.setConnectedRoom(newRoom.code());
                            //ent.setData(Machines.Attachments.MACHINE_COLOR, color);
                            ent.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(color.get()));

                            try {
                                RoomHelper.teleportPlayerIntoRoom(server, sp, newRoom, RoomEntryPoint.playerEnteringMachine(player));
                            } catch (MissingDimensionException e) {
                                throw new RuntimeException(e);
                            }
                        });

                    } catch (MissingDimensionException e) {
                        LoggingUtil.modLog().error("Error occurred while generating new room and machine info for first player entry.", e);
                    }
                } else {
                    LoggingUtil.modLog().fatal("Tried to create and enter an invalidly-registered room. Something went very wrong!");
                }
            });
        }

        return super.use(state, level, pos, player, pHand, pHit);
    }
}

package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlock;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.machine.block.UnboundCompactMachineBlock;
import dev.compactmods.machines.machine.block.UnboundCompactMachineBlockEntity;
import dev.compactmods.machines.machine.capability.IMachineColorCapability;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.item.UnboundCompactMachineItem;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public interface Machines {
    BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties
            .of()
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    Supplier<Item.Properties> MACHINES_ITEM_PROPS = Item.Properties::new;

    interface Blocks {
        RegistryObject<UnboundCompactMachineBlock> UNBOUND_MACHINE = Registries.BLOCKS.register("new_machine", () ->
                new UnboundCompactMachineBlock(MACHINE_BLOCK_PROPS));

        RegistryObject<BoundCompactMachineBlock> BOUND_MACHINE = Registries.BLOCKS.register("machine", () ->
                new BoundCompactMachineBlock(MACHINE_BLOCK_PROPS));

        static void prepare(){}
    }

    interface Items {
        RegistryObject<UnboundCompactMachineItem> UNBOUND_MACHINE = Registries.ITEMS.register("new_machine", () ->
                new UnboundCompactMachineItem(MACHINES_ITEM_PROPS.get()));

        RegistryObject<BoundCompactMachineItem> BOUND_MACHINE = Registries.ITEMS.register("machine", () ->
                new BoundCompactMachineItem(MACHINES_ITEM_PROPS.get()));

        static ItemStack unbound() {
            return unboundColored(0xFFFFFFFF);
        }

        static ItemStack unboundColored(int color){
            return ItemStack.EMPTY;
        }

        static ItemStack boundToRoom(String roomCode) {
            return boundToRoom(roomCode, 0xFFFFFFFF);
        }

        static ItemStack boundToRoom(String roomCode, int color) {
            ItemStack stack = new ItemStack(BOUND_MACHINE.get());
            var tag = stack.getOrCreateTag();
            tag.putString(DataComponents.BOUND_ROOM_CODE, roomCode);
            tag.putInt(DataComponents.MACHINE_COLOR, color);
            //stack.set(Machines.DataComponents.BOUND_ROOM_CODE, roomCode);
            //stack.set(Machines.DataComponents.MACHINE_COLOR, color);
            return stack;
        }

        static ItemStack forNewRoom(ResourceLocation templateID, RoomTemplate template) {
            final var stack = new ItemStack(UNBOUND_MACHINE.get());
            var tag = stack.getOrCreateTag();
            tag.putString(DataComponents.ROOM_TEMPLATE_ID, templateID.toString());
            tag.put(DataComponents.ROOM_TEMPLATE, RoomTemplate.CODEC.encodeStart(NbtOps.INSTANCE, template).get().left().get());
            tag.putInt(DataComponents.MACHINE_COLOR, template.defaultMachineColor().rgb());
            //stack.set(Machines.DataComponents.ROOM_TEMPLATE_ID, templateID);
            //stack.set(Machines.DataComponents.ROOM_TEMPLATE, template);
            //stack.set(Machines.DataComponents.MACHINE_COLOR, template.defaultMachineColor().rgb());
            return stack;
        }

        static void prepare(){}
    }

    interface BlockEntities{
        RegistryObject<BlockEntityType<UnboundCompactMachineBlockEntity>> UNBOUND_MACHINE = Registries.BLOCK_ENTITIES.register("new_machine", () ->
                BlockEntityType.Builder.of(UnboundCompactMachineBlockEntity::new, Blocks.UNBOUND_MACHINE.get()).build(null));
        RegistryObject<BlockEntityType<BoundCompactMachineBlockEntity>> MACHINE = Registries.BLOCK_ENTITIES.register("machine", () ->
                BlockEntityType.Builder.of(BoundCompactMachineBlockEntity::new, Blocks.BOUND_MACHINE.get()).build(null));

        static void prepare(){}
    }

    interface DataComponents{
        String BOUND_ROOM_CODE = "bound_room_code";
        String MACHINE_COLOR = "machine_color";
        String ROOM_TEMPLATE_ID = "room_template_id";
        String ROOM_TEMPLATE = "room_template";
    }

    interface Attachments {
        Capability<IMachineColorCapability> MACHINE_COLOR = CapabilityManager.get(new CapabilityToken<>() {});
    }

    static void prepare(){
        Blocks.prepare();
        Items.prepare();
        BlockEntities.prepare();
    }
}

package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.machine.MachineTranslations;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnboundCompactMachineItem extends BlockItem {
    public UnboundCompactMachineItem(Properties pProperties) {
        super(Machines.Blocks.UNBOUND_MACHINE.get(), pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatableWithFallback(getDescriptionId(pStack), "Compact Machine");
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return Util.makeDescriptionId("machine", getTemplateId(pStack));
    }

    @Override
    public ItemStack getDefaultInstance() {
        var stack = new ItemStack(this);
        var tag = stack.getOrCreateTag();
        tag.putString(Machines.DataComponents.ROOM_TEMPLATE_ID, RoomTemplate.NO_TEMPLATE.toString());
        tag.putInt(Machines.DataComponents.MACHINE_COLOR, CommonColors.WHITE);
        return stack;
    }

    private ResourceLocation getTemplateId(ItemStack stack){
        var tag = stack.getOrCreateTag();
        return new ResourceLocation(tag.getString(Machines.DataComponents.ROOM_TEMPLATE_ID));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        // We need NBT data for the rest of this
        boolean sneaking = Screen.hasShiftDown();

        pTooltip.add(Component.translatableWithFallback(MachineTranslations.IDs.NEW_MACHINE, "New Machine"));

        if (sneaking /*&& registries != null*/) {
            var tag = pStack.getOrCreateTag();
            RoomTemplate.CODEC.decode(NbtOps.INSTANCE, tag.get(Machines.DataComponents.ROOM_TEMPLATE)).result().ifPresent(pair -> {
                pTooltip.add(Component.literal(pair.getFirst().internalDimensions().toString()));
            });
            //stack.addToTooltip(Machines.DataComponents.ROOM_TEMPLATE, context, tooltip::add, flags);
        } else {
            //pTooltip.add(Translations.HINT_HOLD_SHIFT.get());
        }
    }
}

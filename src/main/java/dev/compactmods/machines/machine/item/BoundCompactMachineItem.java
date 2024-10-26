package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.CompactMachinesApi;
import dev.compactmods.machines.api.machine.MachineTranslations;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoundCompactMachineItem extends BlockItem {
    public static final String NBT_ROOM_DIMENSIONS = "room_dimensions";
    private static final String FALLBACK_ID = Util.makeDescriptionId("block", CompactMachinesApi.modRL("bound_machine_fallback"));

    public BoundCompactMachineItem(Properties pProperties) {
        super(Machines.Blocks.BOUND_MACHINE.get(), pProperties);
    }

    /*@Override
    public Component getName(ItemStack pStack) {
        return pStack.getDisplayName();
    }*/

    @Override
    public String getDescriptionId() {
        return FALLBACK_ID;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        /*var roomCode = pStack.get(Machines.DataComponents.BOUND_ROOM_CODE);
        if (roomCode != null) {
            // TODO - Server-synced room name list
            // tooltip.add(TranslationUtil.tooltip(Tooltips.ROOM_NAME, room));
            pTooltip.add(Component.translatableWithFallback(MachineTranslations.IDs.BOUND_TO, "Bound To: %s", roomCode));
        }*/
    }
}

package dev.compactmods.machines.wall;

import dev.compactmods.machines.api.Translations;
import dev.compactmods.machines.api.WallConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockWall extends BlockItem {

    public ItemBlockWall(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (stack.is(WallConstants.TAG_SOLID_WALL_ITEMS)) {
            tooltip.add(Screen.hasShiftDown() ?
                    Translations.UNBREAKABLE_BLOCK.get() : Translations.HINT_HOLD_SHIFT.get());
        }

    }
}

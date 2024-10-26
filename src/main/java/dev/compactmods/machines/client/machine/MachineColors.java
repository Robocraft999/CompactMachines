package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

import java.util.concurrent.atomic.AtomicInteger;

public class MachineColors {
    private static final int DEFAULT = 0xFFFFFFFF;

    public static final ItemColor ITEM = (stack, pTintIndex) -> {
        if (!stack.is(MachineConstants.MACHINE_ITEM)) return DEFAULT;
        var tag = stack.getOrCreateTag();
        return pTintIndex == 0 ? (tag.contains(Machines.DataComponents.MACHINE_COLOR) ? tag.getInt(Machines.DataComponents.MACHINE_COLOR) : DEFAULT) : DEFAULT;
    };

    public static final BlockColor BLOCK = (state, level, pos, tintIndex) -> {
      if (!state.is(MachineConstants.MACHINE_BLOCK) || level == null || pos == null) return DEFAULT;
      var be = level.getBlockEntity(pos);
      if (be != null){
          AtomicInteger color = new AtomicInteger(DEFAULT);
          be.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> color.set(cap.getColor()));
          return tintIndex == 0 ? color.get() : DEFAULT;
      }
      return DEFAULT;
    };
}

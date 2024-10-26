package dev.compactmods.machines.datagen.tags;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.PersonalShrinkingDevice;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider {
    public ItemTagGenerator(PackOutput packOut, BlockTagGenerator blocks, CompletableFuture<HolderLookup.Provider> lookups) {
        super(packOut, lookups, blocks.contentsGetter());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var upgradeTag = tag(CMTags.ROOM_UPGRADE_ITEM);

        upgradeTag.add(MachineRoomUpgrades.CHUNKLOADER.get());

        final var psd = Shrinking.PERSONAL_SHRINKING_DEVICE.get();

        shrinkingDevices(psd);
        machines();
    }

    private void shrinkingDevices(PersonalShrinkingDevice psd) {
        final var cmShrinkTag = tag(PSDTags.ITEM);
        cmShrinkTag.add(psd);
        cmShrinkTag.addOptional(new ResourceLocation("shrink", "shrinking_device"));
    }

    private void machines() {
        var machinesTag = tag(MachineConstants.MACHINE_ITEM);
        var boundMachines = tag(MachineConstants.BOUND_MACHINE_ITEM);
        var unboundMachines = tag(MachineConstants.NEW_MACHINE_ITEM);

        var boundMachineItem = Machines.Items.BOUND_MACHINE.get();
        var unboundMachineItem = Machines.Items.UNBOUND_MACHINE.get();

        machinesTag.add(boundMachineItem);
        machinesTag.add(unboundMachineItem);
        boundMachines.add(boundMachineItem);
        unboundMachines.add(unboundMachineItem);
    }
}

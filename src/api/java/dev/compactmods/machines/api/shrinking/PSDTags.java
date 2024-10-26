package dev.compactmods.machines.api.shrinking;

import dev.compactmods.machines.api.CompactMachinesApi;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface PSDTags {
    /**
     * Marks an item as a personal shrinking device.
     */
    TagKey<Item> ITEM = TagKey.create(Registries.ITEM, CompactMachinesApi.modRL("shrinking_device"));
}

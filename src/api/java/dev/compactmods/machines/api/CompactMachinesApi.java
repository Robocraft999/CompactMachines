package dev.compactmods.machines.api;

import net.minecraft.resources.ResourceLocation;

public interface CompactMachinesApi {
    String MOD_ID = "compactmachines";

    static ResourceLocation modRL(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}

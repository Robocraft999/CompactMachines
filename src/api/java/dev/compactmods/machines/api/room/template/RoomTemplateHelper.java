package dev.compactmods.machines.api.room.template;

import dev.compactmods.machines.api.room.RoomTemplate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;

public class RoomTemplateHelper {
    public static RoomTemplate getTemplate(LevelReader levelReader, ResourceLocation id) {
        return getTemplateOptional(levelReader.registryAccess(), id)
                .orElse(RoomTemplate.INVALID_TEMPLATE);
    }

    public static Optional<RoomTemplate> getTemplateOptional(RegistryAccess registryAccess, ResourceLocation id) {
        return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY)
                .getOptional(id);
    }

    public static Holder.Reference<RoomTemplate> getTemplateHolder(LevelReader levelReader, ResourceLocation id) {
        return getTemplateHolder(levelReader.registryAccess(), id);
    }

    public static Holder.Reference<RoomTemplate> getTemplateHolder(RegistryAccess registryAccess, ResourceLocation id) {
        return registryAccess.registryOrThrow(RoomTemplate.REGISTRY_KEY)
                .getHolderOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));
    }
}

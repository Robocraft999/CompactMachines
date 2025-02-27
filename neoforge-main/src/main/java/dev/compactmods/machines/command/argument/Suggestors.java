package dev.compactmods.machines.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class Suggestors {
   public static final SuggestionProvider<CommandSourceStack> ROOM_TEMPLATES = (ctx, builder) ->
	   SharedSuggestionProvider.suggestResource(getRegistryValues(ctx, RoomTemplate.REGISTRY_KEY), builder);

//    public static final SuggestionProvider<CommandSourceStack> OWNED_ROOM_CODES = (ctx, builder) -> {
//        final var owner = ctx.getSource().getPlayerOrException();
//
//        final var codes = CompactMachines.roomApi().owners()
//                .findByOwner(owner.getUUID())
//                .toList();
//
//        return SharedSuggestionProvider.suggest(codes, builder);
//    };

   public static final SuggestionProvider<CommandSourceStack> ROOM_CODES = (ctx, builder) -> {
	  final var codes = CompactMachines.roomApi()
		  .registrar()
		  .allRoomCodes()
		  .toList();

	  return SharedSuggestionProvider.suggest(codes, builder);
   };

   private static <T> Set<ResourceLocation> getRegistryValues(CommandContext<CommandSourceStack> ctx, ResourceKey<Registry<T>> keyType) {
	  return ctx.getSource().registryAccess()
		  .registryOrThrow(keyType)
		  .keySet();
   }
}

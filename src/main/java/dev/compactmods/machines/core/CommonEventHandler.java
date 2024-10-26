package dev.compactmods.machines.core;

import dev.compactmods.machines.api.CompactMachinesApi;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.machine.capability.IMachineColorCapability;
import dev.compactmods.machines.machine.capability.MachineColorCapabilityImpl;
import dev.compactmods.machines.wall.ProtectedWallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEventHandler {

    @SubscribeEvent
    public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock evt) {
        final var player = evt.getEntity();
        final var pos = evt.getPos();
        final var lev = evt.getLevel();

        final var state = lev.getBlockState(pos);
        if(state.getBlock() instanceof ProtectedWallBlock pwb) {
            if(!pwb.canPlayerBreak(lev, player, pos))
                evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerMachineCaps(RegisterCapabilitiesEvent event){
        event.register(IMachineColorCapability.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<BlockEntity> event){
        event.addCapability(CompactMachinesApi.modRL("color_cap"), new MachineColorCapabilityImpl.Provider());
    }
}

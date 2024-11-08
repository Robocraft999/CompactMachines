package dev.compactmods.machines.client;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.client.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.ui.preview.MachineRoomScreen;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.tunnel.client.TunnelColors;
import dev.compactmods.machines.tunnel.client.TunnelItemColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item colors) {
        colors.register(new TunnelItemColor(), Tunnels.ITEM_TUNNEL.get());
        colors.register(MachineColors.ITEM, Machines.Items.UNBOUND_MACHINE.get(), Machines.Items.BOUND_MACHINE.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(new TunnelColors(), Tunnels.BLOCK_TUNNEL_WALL.get());
        colors.register(MachineColors.BLOCK, Machines.Blocks.UNBOUND_MACHINE.get(), Machines.Blocks.BOUND_MACHINE.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        //MenuScreens.register(UIRegistration.MACHINE_MENU.get(), MachineRoomScreen::new);
        MenuScreens.register(Rooms.Menus.MACHINE_MENU.get(), MachineRoomScreen::new);
    }

    public static void setMachineColor(GlobalPos pos, MachineColor color){
        var mc = Minecraft.getInstance();
        assert mc.level != null;
        if (mc.level.dimension() == pos.dimension()){
            var state = mc.level.getBlockState(pos.pos());
            var blockEntity = mc.level.getBlockEntity(pos.pos());
            if (state.is(MachineConstants.MACHINE_BLOCK)){
                blockEntity.getCapability(Machines.Attachments.MACHINE_COLOR).ifPresent(cap -> cap.setColor(color.rgb()));
                mc.level.sendBlockUpdated(pos.pos(), state, state, Block.UPDATE_ALL_IMMEDIATE);
            }
        }
    }
}

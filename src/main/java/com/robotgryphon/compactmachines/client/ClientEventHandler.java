package com.robotgryphon.compactmachines.client;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onColors(final ColorHandlerEvent.Block colors) {
        colors.getBlockColors().register(new TunnelColors(), Registrations.BLOCK_TUNNEL_WALL.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        RenderType cutout = RenderType.getCutoutMipped();
        RenderTypeLookup.setRenderLayer(Registrations.BLOCK_TUNNEL_WALL.get(), cutout);
    }
}

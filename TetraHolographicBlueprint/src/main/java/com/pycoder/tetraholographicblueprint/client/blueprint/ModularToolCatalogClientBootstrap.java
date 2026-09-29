package com.pycoder.tetraholographicblueprint.client.blueprint;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModularToolCatalogClientBootstrap {
    private ModularToolCatalogClientBootstrap() {
    }

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ModularToolCatalogClientBootstrap::onRegisterClientReloadListeners);

        MinecraftForge.EVENT_BUS.addListener(ModularToolCatalogClientBootstrap::onClientLoggingIn);
        MinecraftForge.EVENT_BUS.addListener(ModularToolCatalogClientBootstrap::onClientLoggingOut);
    }

    private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new CatalogReloadListener());
    }

    private static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ModularToolCatalog.refresh(ForgeRegistries.ITEMS.getValues());
    }

    private static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ModularToolCatalog.clear();
    }

    private static final class CatalogReloadListener extends SimplePreparableReloadListener<Void> {
        @Override
        protected Void prepare(net.minecraft.server.packs.resources.ResourceManager resourceManager,
                               net.minecraft.util.profiling.ProfilerFiller profiler) {
            ModularToolCatalog.clear();
            return null;
        }

        @Override
        protected void apply(Void prepared, net.minecraft.server.packs.resources.ResourceManager resourceManager,
                             net.minecraft.util.profiling.ProfilerFiller profiler) {
            ModularToolCatalog.refresh(ForgeRegistries.ITEMS.getValues());
        }
    }
}

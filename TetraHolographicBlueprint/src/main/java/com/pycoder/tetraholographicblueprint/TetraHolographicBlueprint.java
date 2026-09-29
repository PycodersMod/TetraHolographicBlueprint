package com.pycoder.tetraholographicblueprint;

import com.mojang.logging.LogUtils;
import com.pycoder.tetraholographicblueprint.common.ModConstants;
import com.pycoder.tetraholographicblueprint.client.blueprint.ModularToolCatalogClientBootstrap;
import org.slf4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(TetraHolographicBlueprint.MOD_ID)
public class TetraHolographicBlueprint {
    public static final String MOD_ID = ModConstants.MOD_ID;
    private static final Logger LOGGER = LogUtils.getLogger();

    public TetraHolographicBlueprint() {
        LOGGER.info("{} loaded", MOD_ID);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ModularToolCatalogClientBootstrap::register);
    }
}

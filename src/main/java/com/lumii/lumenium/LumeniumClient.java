package com.lumii.lumenium;

import com.lumii.lumenium.testing.LibEffects;
import com.lumii.lumenium.testing.LibItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class LumeniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if(FabricLoader.getInstance().isDevelopmentEnvironment()){
            LibItems.init();
            LibEffects.init();
        }
    }
}

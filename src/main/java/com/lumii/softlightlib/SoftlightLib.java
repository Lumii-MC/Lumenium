package com.lumii.softlightlib;

import com.lumii.softlightlib.testing.LibItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoftlightLib implements ModInitializer {
	public static final String MOD_ID = "softlightlib";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if(FabricLoader.getInstance().isDevelopmentEnvironment()){
			LibItems.init();
		}

		LOGGER.info("SoftlightLib");
	}
}
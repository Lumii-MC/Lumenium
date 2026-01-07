package com.lumii.softlightlib.utils.generic;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

import java.util.UUID;

public class CrashByUUID {

    private static UUID targetUUID = null;
    private static boolean registered = false;

    /**
     * Sets the UUID that should cause a crash.
     * Automatically registers the tick event the first time it's called.
     */
    public static void uuid(String uuidString) {
        targetUUID = UUID.fromString(uuidString);

        if (!registered) {
            registered = true;

            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.player != null && targetUUID != null) {

                    UUID playerId = client.player.getUuid();

                    if (playerId.equals(targetUUID)) {
                        throw new CrashException(
                                new CrashReport("Quitting game...", new Throwable())
                        );
                    }
                }
            });
        }
    }
}

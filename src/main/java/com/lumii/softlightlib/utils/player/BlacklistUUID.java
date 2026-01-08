package com.lumii.softlightlib.utils.player;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BlacklistUUID {

    private static final Logger LOGGER = LoggerFactory.getLogger("SoftlightLib-BlacklistUUID");

    private static UUID targetUUID = null;
    private static boolean registered = false;
    private static boolean checkedOnce = false;

    /**
     * Sets the UUID that should cause a crash.
     * Automatically registers the tick event the first time it's called.
     */
    public static void uuid(String uuid) {
        targetUUID = UUID.fromString(uuid);

        if (!registered) {
            registered = true;

            ClientTickEvents.END_CLIENT_TICK.register(client -> {

                if(checkedOnce) return;

                if (client.player != null && targetUUID != null) {

                    UUID playerId = client.player.getUuid();
                    checkedOnce = true;

                    if (playerId.equals(targetUUID)) {

                        String message = "User is blacklisted.";

                        LOGGER.info(message);

                        CrashReport report = CrashReport.create(new RuntimeException(message), message);

                        throw new CrashException(report);
                    }
                }
            });
        }
    }
}
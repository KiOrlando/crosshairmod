package zaorlando.crosshairmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrosshairMod implements ModInitializer, ClientModInitializer
{
    public static final String MOD_ID = "crosshairmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Used to know when custom rendering is needed.
    public static boolean activeCrosshairIsVanilla;

    // The currently rendered crosshair.
    // Should only be updated by events.
    public static CrosshairType activeCrosshair;

    @Override
    public void onInitialize()
    {
    }

    @Override
    public void onInitializeClient()
    {
    }
}

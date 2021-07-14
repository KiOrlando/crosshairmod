package zaorlando.crosshairmod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zaorlando.crosshairmod.config.ModConfig;

public class CrosshairMod implements ModInitializer, ClientModInitializer
{
    public static final CrosshairType DEFAULT_CROSSHAIR = CrosshairType.DOT;

    public static final String MOD_ID = "crosshairmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize()
    {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    @Override
    public void onInitializeClient()
    {
    }
}
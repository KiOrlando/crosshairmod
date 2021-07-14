package zaorlando.crosshairmod;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.util.Identifier;
import zaorlando.crosshairmod.config.ModConfig;

public enum CrosshairType
{
    DOT("crosshair_dot"),
    BLOCK("crosshair_block"),
    ERROR("crosshair_error"),
    ATTACK("crosshair_attack");

    private final Identifier identifier;

    CrosshairType(String id)
    {
        this.identifier = new Identifier(CrosshairMod.MOD_ID, "textures/gui/" + id + ".png");
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }

    public boolean isEnabled()
    {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        return switch(this) {
            case DOT    -> config.enableDot;
            case BLOCK  -> config.enableBlock;
            case ERROR  -> config.enableError;
            case ATTACK -> config.enableAttack;
        };
    }
}
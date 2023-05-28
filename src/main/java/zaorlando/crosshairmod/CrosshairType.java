package zaorlando.crosshairmod;

import net.minecraft.util.Identifier;

public enum CrosshairType
{
    DOT("crosshair_dot"),
    BLOCK_NO_DROP("crosshair_error"),
    BLOCK_HARVEST("crosshair_block"),
    BLOCK_SILK("crosshair_block"),
    ERROR("crosshair_error"),
    ATTACK("crosshair_attack");

    private final Identifier identifier;

    CrosshairType(String id)
    {
        this.identifier = new Identifier(Mod.MOD_ID, "textures/gui/" + id + ".png");
    }

    public Identifier getIdentifier()
    {
        return identifier;
    }
}
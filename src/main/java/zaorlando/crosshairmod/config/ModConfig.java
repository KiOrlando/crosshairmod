package zaorlando.crosshairmod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import zaorlando.crosshairmod.CrosshairMod;

@Config(name = CrosshairMod.MOD_ID)
public class ModConfig implements ConfigData
{
    @Comment("""
             Appears when no actions are available.
             This is the default, disabling this is
             supported but not recommended.""")
    public boolean enableDot = true;

    @Comment("""
             Appears when looking at a block you can
             break or place.""")
    public boolean enableBlock = true;

    @Comment("""
             Appears when where there is an action
             available but it cannot be preformed
             successfully.""")
    public boolean enableError = true;

    @Comment("""
             Appears when looking at an entity or when
             you are holding a bow or crossbow.""")
    public boolean enableAttack = true;
}

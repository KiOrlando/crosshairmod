package zaorlando.crosshairmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zaorlando.crosshairmod.CrosshairMod;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    // Looking at mob
    private static final Identifier GUI_CROSSHAIR1_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair1.png");
    // Looking at block
    private static final Identifier GUI_CROSSHAIR2_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair2.png");
    // Looking at block that cant be harvested
    private static final Identifier GUI_CROSSHAIR4_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair4.png");
    // Looking at nothing
    private static final Identifier GUI_CROSSHAIR3_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair3.png");

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Redirect(method = "renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
                       ordinal = 0))
    private void crosshairmod_renderCrosshairRedirect(InGameHud inGameHud, MatrixStack matrices, int x, int y, int u, int v, int width, int height)
    {
        var client = MinecraftClient.getInstance();

        if(client.crosshairTarget != null && client.world != null && client.player != null)
        {
            switch(client.crosshairTarget.getType())
            {
                // Looking at entity
                case ENTITY -> {
                    // var hit = ((EntityHitResult)client.crosshairTarget);
                    // var entity = client.targetedEntity;
                    RenderSystem.setShaderTexture(0, GUI_CROSSHAIR1_TEXTURE);
                }
                // Looking at block
                case BLOCK -> {
                    var hit = ((BlockHitResult)client.crosshairTarget);

                    // Check is player can collect the block they are looking at.
                    if(client.player.isCreative() || client.player.canHarvest(client.world.getBlockState(hit.getBlockPos())))
                        RenderSystem.setShaderTexture(0, GUI_CROSSHAIR2_TEXTURE);
                    else
                        RenderSystem.setShaderTexture(0, GUI_CROSSHAIR4_TEXTURE);
                }
                // Looking at nothing
                default -> {
                    var hit = client.crosshairTarget;
                    RenderSystem.setShaderTexture(0, GUI_CROSSHAIR3_TEXTURE);
                }
            }
        }
        else if(client.crosshairTarget == null)
            CrosshairMod.LOGGER.error("Could not change crosshair because the 'MinecraftClient.getInstance().crosshairTarget' object is null.");
        else if(client.world == null)
            CrosshairMod.LOGGER.error("Could not change crosshair because the 'MinecraftClient.getInstance().world' object is null.");
        else
            CrosshairMod.LOGGER.error("Could not change crosshair because the 'MinecraftClient.getInstance().player' object is null.");

        InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);

        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }
}

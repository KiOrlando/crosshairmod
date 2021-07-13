package zaorlando.crosshairmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zaorlando.crosshairmod.CrosshairMod;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    // Looking at mob
    private static final Identifier GUI_CROSSHAIR1_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair1.png");
    // Looking at block
    private static final Identifier GUI_CROSSHAIR2_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair2.png");
    // Looking at nothing
    private static final Identifier GUI_CROSSHAIR3_TEXTURE = new Identifier(CrosshairMod.MOD_ID, "textures/gui/crosshair3.png");

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,
            method="renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At(value = "INVOKE",
                     shift = At.Shift.BEFORE,
                     target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void renderCrosshairMixinBefore(MatrixStack ms, CallbackInfo ci)
    {
        if(MinecraftClient.getInstance().crosshairTarget != null)
        {
            switch(MinecraftClient.getInstance().crosshairTarget.getType())
            {
                // Looking at entity
                case ENTITY -> {
                    RenderSystem.setShaderTexture(0, GUI_CROSSHAIR1_TEXTURE);
                }
                // Looking at block
                case BLOCK -> {
                    RenderSystem.setShaderTexture(0, GUI_CROSSHAIR2_TEXTURE);
                }
                // Looking at nothing
                default -> {
                    RenderSystem.setShaderTexture(0, GUI_CROSSHAIR3_TEXTURE);
                }
            }
        }
        else
        {
            CrosshairMod.LOGGER.error("Could not change crosshair because the 'MinecraftClient.getInstance().crosshairTarget' object is null.");
        }
        InGameHud.drawTexture(ms, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);
    }

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At(value = "INVOKE",
                     shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void renderCrosshairMixinAfter(MatrixStack ms, CallbackInfo ci)
    {
        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
    }
}

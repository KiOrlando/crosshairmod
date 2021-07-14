package zaorlando.crosshairmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zaorlando.crosshairmod.CrosshairMod;
import zaorlando.crosshairmod.CrosshairType;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void crosshairmod$renderCrosshairRedirect(InGameHud inGameHud, MatrixStack matrices, int x, int y, int u, int v, int width, int height)
    {
        CrosshairType crosshair = getCrosshair();

        if(crosshair.isEnabled())
        {
            RenderSystem.setShaderTexture(0, crosshair.getIdentifier());
            InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);
        }
        else if(CrosshairMod.DEFAULT_CROSSHAIR.isEnabled())
        {
            RenderSystem.setShaderTexture(0, CrosshairMod.DEFAULT_CROSSHAIR.getIdentifier());
            InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);
        }

        RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
        if(!crosshair.isEnabled() && !CrosshairMod.DEFAULT_CROSSHAIR.isEnabled())
            inGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15);
    }

    private static CrosshairType getCrosshair()
    {
        MinecraftClient client = MinecraftClient.getInstance();

        if(client == null)
            return CrosshairMod.DEFAULT_CROSSHAIR;

        var target = client.crosshairTarget;
        var player = client.player;
        var world = client.world;

        if(target == null || world == null || player == null)
            return CrosshairMod.DEFAULT_CROSSHAIR;

        // If holding bow or crossbow show the attack crosshair
        // Suggested by: vos6434
        if(player.getMainHandStack().isOf(Items.BOW) || player.getOffHandStack().isOf(Items.BOW))
        {
            if(player.isCreative() || player.getInventory().contains(ItemTags.ARROWS))
                return CrosshairType.ATTACK;
            return CrosshairType.ERROR;
        }
        if(player.getMainHandStack().isOf(Items.CROSSBOW) || player.getOffHandStack().isOf(Items.CROSSBOW))
        {
            if(player.isCreative() ||
               player.getInventory().contains(ItemTags.ARROWS) ||
               player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) ||
               player.getOffHandStack().isOf(Items.FIREWORK_ROCKET) ||
               CrossbowItem.isCharged(player.getMainHandStack()))
                return CrosshairType.ATTACK;
            return CrosshairType.ERROR;
        }

        return switch(target.getType()) {
            case MISS   -> CrosshairMod.DEFAULT_CROSSHAIR;
            case ENTITY -> CrosshairType.ATTACK;
            case BLOCK  -> {
                BlockState state = world.getBlockState(((BlockHitResult)target).getBlockPos());

                if(player.isCreative() || player.canHarvest(state))
                    yield CrosshairType.BLOCK;
                yield CrosshairType.ERROR;
            }
        };
    }
}

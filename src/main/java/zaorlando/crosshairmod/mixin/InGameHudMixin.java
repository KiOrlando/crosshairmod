package zaorlando.crosshairmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.item.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zaorlando.crosshairmod.CrosshairMod;
import zaorlando.crosshairmod.CrosshairType;

import java.util.Set;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Shadow public abstract int getTicks();

    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void crosshairmod$renderCrosshairRedirect(InGameHud inGameHud, MatrixStack matrices, int x, int y, int u, int v, int width, int height)
    {
        // Checks if a custom crosshair is supposed to be rendered.
        if(!CrosshairMod.activeCrosshairIsVanilla)
        {
            // Sets the shader texture to the image of the custom crosshair.
            RenderSystem.setShaderTexture(0, CrosshairMod.activeCrosshair.getIdentifier());

            // Draws the custom crosshair to the screen. This method call is
            // different to the vanilla call due to the different dimensions
            // of the texture files.
            InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);

            // This needs to be called after the custom crosshair is rendered
            // to set the shader texture back to what it is expected to be.
            RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
        }
        else
        {
            // Renders the vanilla crosshair when no custom crosshair is active.
            inGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15);
        }
//        if(crosshair.isEnabled())
//        {
//            RenderSystem.setShaderTexture(0, crosshair.getIdentifier());
//            InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);
//        }
//        else if(CrosshairMod.DEFAULT_CROSSHAIR.isEnabled())
//        {
//            RenderSystem.setShaderTexture(0, CrosshairMod.DEFAULT_CROSSHAIR.getIdentifier());
//            InGameHud.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15, 15, 15);
//        }
//        if(!crosshair.isEnabled() && !CrosshairMod.DEFAULT_CROSSHAIR.isEnabled())

    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void crosshairmod$tick(CallbackInfo ci)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult target = client.crosshairTarget;

        if(target != null)
        {
            // By default, assume that there will be a custom crosshair used.
            // This value is updated in the default case of the switch statement
            // below.
            CrosshairMod.activeCrosshairIsVanilla = false;

            switch(target.getType())
            {
                case MISS:
                    doMissTargetLogic(client);
                case ENTITY:
                    setCrosshair(CrosshairType.ATTACK);
                case BLOCK:
                    doBlockTargetLogic(client, target);
                default:
                    CrosshairMod.activeCrosshairIsVanilla = true;
            }

            // Edge case for bows
            if(checkBowEdgeCase(client))
                setCrosshair(CrosshairType.ATTACK);
        }
    }

    private static void setCrosshair(CrosshairType c)
    {
        CrosshairMod.activeCrosshair = c;
    }

    // Simple helper method for checking if a bow or crossbow is loaded
    private static boolean checkBowEdgeCase(MinecraftClient client)
    {
        ClientPlayerEntity player = client.player;

        if(player == null)
            return false;

        ItemStack mHand = player.getMainHandStack();
        ItemStack oHand = player.getOffHandStack();

        ItemStack toTest = null;

        // Check if the player is holding a bow or crossbow.
        if(mHand.isOf(Items.BOW) || mHand.isOf(Items.CROSSBOW))
            toTest = mHand;
        if(oHand.isOf(Items.BOW) || oHand.isOf(Items.CROSSBOW))
            toTest = oHand;

        if(toTest == null)
            return false;

        // Check if the bow/crossbow is charged.
        // TODO: Maybe add an indicator when the bow is fully charged?
        if(toTest.isOf(Items.BOW))
            return !player.getArrowType(toTest).isEmpty();

        if(toTest.isOf(Items.CROSSBOW))
            return CrossbowItem.isCharged(toTest);

        return false;
    }

    private void doMissTargetLogic(MinecraftClient client)
    {
        // Default for miss target.
        setCrosshair(CrosshairType.DOT);
    }

    // This method is used to preform the logic needed when the crosshair is on a block.
    private void doBlockTargetLogic(MinecraftClient client, HitResult target)
    {
        // Default for block target.
        setCrosshair(CrosshairType.ERROR);

        ClientPlayerEntity player = client.player;

        if(player == null)
            return;

        BlockState state =  player.getWorld().getBlockState(((BlockHitResult)target).getBlockPos());

        // Check if the player can mine the block they are looking at.
        if(player.canHarvest(state) || player.isCreative())
            setCrosshair(CrosshairType.BLOCK);
    }

//    @Deprecated
//    private static CrosshairType getCrosshair()
//    {
//        MinecraftClient client = MinecraftClient.getInstance();
//
//        if(client == null)
//            return CrosshairMod.DEFAULT_CROSSHAIR;
//
//        var target = client.crosshairTarget;
//        var player = client.player;
//        var world = client.world;
//
//        if(target == null || world == null || player == null)
//            return CrosshairMod.DEFAULT_CROSSHAIR;
//
//        // If holding bow or crossbow show the attack crosshair
//        // Suggested by: vos6434
//        if(player.getMainHandStack().isOf(Items.BOW) || player.getOffHandStack().isOf(Items.BOW))
//        {
//            if(player.isCreative() || player.getInventory().contains(ItemTags.ARROWS))
//                return CrosshairType.ATTACK;
//            return CrosshairType.ERROR;
//        }
//        if(player.getMainHandStack().isOf(Items.CROSSBOW) || player.getOffHandStack().isOf(Items.CROSSBOW))
//        {
//            if(player.isCreative() ||
//               player.getInventory().contains(ItemTags.ARROWS) ||
//               player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) ||
//               player.getOffHandStack().isOf(Items.FIREWORK_ROCKET) ||
//               CrossbowItem.isCharged(player.getMainHandStack()))
//                return CrosshairType.ATTACK;
//            return CrosshairType.ERROR;
//        }
//
//        return switch(target.getType()) {
//            case MISS   -> CrosshairMod.DEFAULT_CROSSHAIR;
//            case ENTITY -> CrosshairType.ATTACK;
//            case BLOCK  -> {
//                BlockState state = world.getBlockState(((BlockHitResult)target).getBlockPos());
//
//                if(player.isCreative() || player.canHarvest(state))
//                    yield CrosshairType.BLOCK;
//                yield CrosshairType.ERROR;
//            }
//        };
//    }
}

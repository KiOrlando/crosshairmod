package zaorlando.crosshairmod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zaorlando.crosshairmod.CrosshairType;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin
{
    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    private boolean shouldDisplayVanillaCrosshair;
    private CrosshairType activeCrosshair;

    @Redirect(method = "renderCrosshair", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    private void crosshairmod$renderCrosshair(MatrixStack matrices, int x, int y, int u, int v, int width, int height)
    {
        // Checks if a custom crosshair is supposed to be rendered and that the custom crosshair is not null.
        if(this.activeCrosshair != null && !this.shouldDisplayVanillaCrosshair)
        {
            // Sets the shader texture to the image of the custom crosshair.
            RenderSystem.setShaderTexture(0, this.activeCrosshair.getIdentifier());
            
            // Draws the custom crosshair to the screen. This method call is
            // different to the vanilla call due to the different dimensions
            // of the texture files.
            InGameHud.drawTexture(matrices, x, y, 0, 0, 15, 15, 15, 15);

            // This needs to be called after the custom crosshair is rendered
            // to set the shader texture back to what it is expected to be.
            RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
        }
        else
        {
            // Renders the vanilla crosshair when no custom crosshair is active.
            InGameHud.drawTexture(matrices, x, y, u, v, width, height);
        }
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
            this.shouldDisplayVanillaCrosshair = false;

            switch(target.getType())
            {
                case MISS   -> setCrosshair(getPreferredMissCrosshair(client));
                case ENTITY -> setCrosshair(CrosshairType.ATTACK);
                case BLOCK  -> setCrosshair(getPreferredBlockCrosshair(client, target));
                default     -> this.shouldDisplayVanillaCrosshair = true;
            }

            // Check for the bow edge case. If detected,
            // set the crosshair to the attack crosshair.
            if(checkBowEdgeCase(client))
                setCrosshair(CrosshairType.ATTACK);
        }
    }

    /**
     * A small helper function used to easily assigning the value of {@link InGameHudMixin#activeCrosshair}.
     * @param c A {@link CrosshairType} enum value that will be assigned to {@link InGameHudMixin#activeCrosshair}.
     */
    private void setCrosshair(CrosshairType c)
    {
        this.activeCrosshair = c;
    }

    /**
     * Simple helper method for checking if a bow or crossbow is loaded
     * @return Returns true if the edge case is detected.
     */
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
            return false;
//            return !player.getArrowType(toTest).isEmpty();

        if(toTest.isOf(Items.CROSSBOW))
            return CrossbowItem.isCharged(toTest);

        return false;
    }

    /**
     * This method is used to get the correct crosshair to display when looking at nothing.
     * @return Returns the correct crosshair to display.
     */
    private CrosshairType getPreferredMissCrosshair(MinecraftClient client)
    {
        // Return the default for the miss target case.
        return CrosshairType.DOT;
    }

    /**
     * This method is used to get the correct crosshair to display when looking at a block.
     * @return Returns the correct crosshair to display.
     */
    private CrosshairType getPreferredBlockCrosshair(MinecraftClient client, HitResult target)
    {
        final ClientPlayerEntity player = client.player;

        // If the player is null then it can't be used below.
        // Return the output to prevent an exception.
        if(player == null)
            return CrosshairType.ERROR;

        // Get the block the player is looking at.
        BlockState state = player.getWorld().getBlockState(((BlockHitResult)target).getBlockPos());

        // The player can mine whatever they are looking at if they are in creative
        if(player.isCreative())
            return CrosshairType.BLOCK_HARVEST;

        // Check if the player can mine the block they are looking at.
        if(state.getBlock().getHardness() >= 0 && player.canHarvest(state))
            return CrosshairType.BLOCK_HARVEST;

        // Return for anything that happens to not pass any other cases.
        return CrosshairType.BLOCK_NO_DROP;
    }
}

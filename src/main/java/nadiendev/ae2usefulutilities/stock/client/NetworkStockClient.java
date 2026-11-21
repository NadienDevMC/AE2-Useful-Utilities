package nadiendev.ae2usefulutilities.stock.client;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import nadiendev.ae2usefulutilities.stock.network.OpenCraftC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class NetworkStockClient {

    private static final long CRAFT_COOLDOWN_MS = 400;

    private long lastCraftRequest;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        boolean craftButton = event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                || event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
        if (craftButton && craftModifiers()
                && requestCraft(HoverKeys.under(event.getScreen(), event.getMouseX(), event.getMouseY()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScreenScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (craftModifiers()
                && requestCraft(HoverKeys.under(event.getScreen(), event.getMouseX(), event.getMouseY()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldMiddleClick(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS
                || minecraft.screen != null || minecraft.level == null || !craftModifiers()) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (requestCraft(blockKey(minecraft.level, blockHit.getBlockPos(), minecraft.player))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        NetworkStockCache.clear();
    }

    private static boolean craftModifiers() {
        return Minecraft.getInstance().hasControlDown() && Minecraft.getInstance().hasShiftDown();
    }

    @Nullable
    private static AEKey blockKey(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state, false, player);
        return stack.isEmpty() ? null : AEItemKey.of(stack);
    }

    private boolean requestCraft(@Nullable AEKey key) {
        if (key == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastCraftRequest >= CRAFT_COOLDOWN_MS) {
            lastCraftRequest = now;
            ClientPacketDistributor.sendToServer(new OpenCraftC2SPacket(key));
        }
        return true;
    }
}

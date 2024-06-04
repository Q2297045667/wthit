package mcp.mobius.waila.api.component;

import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaConfig;
import mcp.mobius.waila.api.__internal__.ApiSide;
import mcp.mobius.waila.api.__internal__.IApiService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Component that renders key-value pair that would be aligned at the colon.
 */
@ApiSide.ClientOnly
public class PairComponent implements ITooltipComponent {

    public PairComponent(Component key, Component value) {
        this(new WrappedComponent(key), new WrappedComponent(value));
    }

    public PairComponent(ITooltipComponent key, ITooltipComponent value) {
        this.key = key;
        this.value = value;

        height = Math.max(key.getHeight(), value.getHeight());
    }

    public final ITooltipComponent key, value;
    private final int height;

    @Override
    public int getWidth() {
        return getColonOffset() + getColonWidth() + value.getWidth();
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public @Nullable Component getNarration() {
        var keyNarration = key.getNarration();
        var valueNarration = value.getNarration();
        if (keyNarration != null) {
            if (valueNarration != null) return keyNarration.copy().append(valueNarration);
            else return keyNarration;
        }
        return valueNarration;
    }

    @Override
    public void render(GuiGraphics ctx, int x, int y, float delta) {
        var offset = key.getHeight() < height ? (height - key.getHeight()) / 2 : 0;
        IApiService.INSTANCE.renderComponent(ctx, key, x, y + offset, delta);

        var font = Minecraft.getInstance().font;
        offset = font.lineHeight < height ? (height - font.lineHeight) / 2 : 0;
        ctx.drawString(font, ": ", x + getColonOffset(), y + offset, IWailaConfig.get().getOverlay().getColor().getTheme().getDefaultTextColor());

        offset = value.getHeight() < height ? (height - value.getHeight()) / 2 : 0;
        IApiService.INSTANCE.renderComponent(ctx, value, x + getColonOffset() + getColonWidth(), y + offset, delta);
    }

    private int getColonOffset() {
        return IApiService.INSTANCE.getPairComponentColonOffset();
    }

    private int getColonWidth() {
        return IApiService.INSTANCE.getColonFontWidth();
    }

}

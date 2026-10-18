package com.viltrumitegens.client;

import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import com.viltrumitegens.network.SelectPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

/** Puan havuzundan kendi genetigini kurdugun ekran. Shift = 5'er puan. */
public class CustomGenesScreen extends Screen {
    private static final int W = 330;
    private static final int H = 226;
    private static final int ACCENT = 0xFFFFD24D;

    private final Screen parent;
    private final int[] points = new int[GensStat.COUNT];
    private int left;
    private int top;

    public CustomGenesScreen(Screen parent) {
        super(Component.translatable("screen.viltrumite_gens.custom_title"));
        this.parent = parent;
    }

    private int remaining() {
        int sum = 0;
        for (int p : points) sum += p;
        return ClientHooks.pool - sum;
    }

    private void change(int index, int delta) {
        int target = points[index] + delta;
        if (target < ClientHooks.minPerGene) target = ClientHooks.minPerGene;
        int diff = target - points[index];
        if (diff > remaining()) target = points[index] + remaining();
        points[index] = target;
    }

    private int step() {
        return hasShiftDown() ? 5 : 1;
    }

    private int rowY(int i) {
        return top + 54 + i * 24;
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;

        for (int i = 0; i < GensStat.COUNT; i++) {
            final int idx = i;
            int y = rowY(i);
            this.addRenderableWidget(Button.builder(Component.literal("-"), b -> change(idx, -step()))
                    .bounds(left + 88, y, 18, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("+"), b -> change(idx, step()))
                    .bounds(left + 214, y, 18, 18).build());
        }

        int by = top + H - 30;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.confirm"), b -> {
                    GensNetwork.CHANNEL.sendToServer(new SelectPacket(true, 1, points.clone()));
                    this.minecraft.setScreen(null);
                })
                .bounds(left + 12, by, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.reset"),
                        b -> java.util.Arrays.fill(points, 0))
                .bounds(left + 116, by, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.back"),
                        b -> this.minecraft.setScreen(parent))
                .bounds(left + 220, by, 98, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        // Panel
        g.fill(left, top, left + W, top + H, 0xE0101018);
        g.fill(left, top, left + W, top + 3, ACCENT);
        g.renderOutline(left, top, W, H, 0xFF303040);

        g.drawCenteredString(this.font, this.title, left + W / 2, top + 11, 0xFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("screen.viltrumite_gens.custom_desc"),
                left + W / 2, top + 24, 0xFF999999);

        int rem = remaining();
        g.drawCenteredString(this.font,
                Component.translatable("screen.viltrumite_gens.points_left", rem),
                left + W / 2, top + 38, rem == 0 ? 0xFF55FF55 : 0xFFFFFF55);

        int pool = Math.max(1, ClientHooks.pool);
        int min = ClientHooks.minPerGene;

        for (int i = 0; i < GensStat.COUNT; i++) {
            GensStat s = GensStat.values()[i];
            int y = rowY(i);
            int p = points[i];

            g.drawString(this.font, Component.translatable(s.langKey()), left + 12, y + 5, 0xFFDDDDDD, false);

            // Orta noktali cubuk: sag = guc, sol = zayiflik
            int bx = left + 110;
            int bw = 100;
            int cx = bx + bw / 2;
            g.fill(bx, y + 6, bx + bw, y + 12, 0xFF282838);
            if (p > 0) {
                int w = Math.min(bw / 2, Math.round(p / (float) pool * (bw / 2f)));
                g.fill(cx, y + 6, cx + w, y + 12, 0xFF55FF55);
            } else if (p < 0 && min < 0) {
                int w = Math.min(bw / 2, Math.round(p / (float) min * (bw / 2f)));
                g.fill(cx - w, y + 6, cx, y + 12, 0xFFFF5555);
            }
            g.fill(cx, y + 4, cx + 1, y + 14, 0xFFFFFFFF);

            String val = String.format(Locale.ROOT, "%+d (%+.0f%%)", p, p * ClientHooks.percentPerPoint);
            int vc = p > 0 ? 0xFF55FF55 : (p < 0 ? 0xFFFF5555 : 0xFFAAAAAA);
            g.drawString(this.font, val, left + 238, y + 5, vc, false);
        }

        // Ipucu
        List<FormattedCharSequence> hint = this.font.split(
                FormattedText.of(Component.translatable("screen.viltrumite_gens.hint").getString()), W - 24);
        int hy = top + H - 52;
        for (int i = 0; i < Math.min(2, hint.size()); i++) {
            g.drawString(this.font, hint.get(i), left + 12, hy + i * 10, 0xFF777777, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

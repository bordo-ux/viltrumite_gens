package com.viltrumitegens.client;

import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import com.viltrumitegens.network.SelectPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Puan havuzundan kendi genetigini kurdugun ekran. Shift = 5'er puan. */
public class CustomGenesScreen extends Screen {
    private final Screen parent;
    private final int[] points = new int[GensStat.COUNT];

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

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 70;

        for (int i = 0; i < GensStat.COUNT; i++) {
            final int idx = i;
            int y = top + i * 24;
            this.addRenderableWidget(Button.builder(Component.literal("-"), b -> change(idx, -step()))
                    .bounds(cx + 20, y, 20, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("+"), b -> change(idx, step()))
                    .bounds(cx + 44, y, 20, 20).build());
        }

        int by = top + GensStat.COUNT * 24 + 12;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.confirm"), b -> {
                    GensNetwork.CHANNEL.sendToServer(new SelectPacket(true, 1, points.clone()));
                    this.minecraft.setScreen(null);
                })
                .bounds(cx - 105, by, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.back"),
                        b -> this.minecraft.setScreen(parent))
                .bounds(cx + 5, by, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int top = this.height / 2 - 70;

        g.drawCenteredString(this.font, this.title, cx, top - 40, 0xFFFFFF);
        g.drawCenteredString(this.font,
                Component.translatable("screen.viltrumite_gens.points_left", remaining()),
                cx, top - 24, remaining() == 0 ? 0x55FF55 : 0xFFFF55);

        for (int i = 0; i < GensStat.COUNT; i++) {
            int y = top + i * 24 + 6;
            GensStat s = GensStat.values()[i];
            g.drawString(this.font, Component.translatable(s.langKey()), cx - 120, y, 0xFFFFFF);
            String val = String.format(Locale.ROOT, "%d (%+.0f%%)", points[i], points[i] * ClientHooks.percentPerPoint);
            g.drawString(this.font, val, cx - 30, y, points[i] < 0 ? 0xFF5555 : 0xAAAAAA);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

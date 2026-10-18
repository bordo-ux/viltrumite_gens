package com.viltrumitegens.client;

import com.viltrumitegens.data.GensLevel;
import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import com.viltrumitegens.network.SelectPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

/** Seviye secim ekrani: solda seviye listesi, sagda secili seviyenin ayrintilari ve istatistik cubuklari. */
public class LevelSelectScreen extends Screen {
    private static final int W = 340;
    private static final int H = 214;
    private static final int LIST_W = 114;

    /** Seviye renkleri (GensLevel siralamasiyla ayni). */
    private static final int[] COLORS = {0xAAAAAA, 0x55FF55, 0x55FFFF, 0xFFAA00, 0xFF5555, 0xFF55FF};

    private record Row(String key, ToDoubleFunction<GensLevel> getter) {}

    private static final List<Row> ROWS = List.of(
            new Row("stat.viltrumite_gens.health", l -> l.health),
            new Row("stat.viltrumite_gens.damage", l -> l.damage),
            new Row("stat.viltrumite_gens.durability", l -> l.durability),
            new Row("stat.viltrumite_gens.speed", l -> l.speed),
            new Row("stat.viltrumite_gens.regen", l -> l.regen),
            new Row("stat.viltrumite_gens.power", l -> l.power),
            new Row("stat.viltrumite_gens.area", l -> l.area),
            new Row("stat.viltrumite_gens.destruction", l -> l.destruction)
    );

    private GensLevel selected = GensLevel.SOLDIER;
    private int left;
    private int top;

    public LevelSelectScreen() {
        super(Component.translatable("screen.viltrumite_gens.title"));
    }

    private int accent(GensLevel l) {
        return 0xFF000000 | COLORS[l.ordinal()];
    }

    @Override
    protected void init() {
        left = (this.width - W) / 2;
        top = (this.height - H) / 2;

        int y = top + 34;
        for (GensLevel lvl : GensLevel.values()) {
            Component label = Component.literal(lvl.id + "  ")
                    .append(Component.translatable(lvl.langKey() + ".short"));
            this.addRenderableWidget(Button.builder(label, b -> selected = lvl)
                    .bounds(left + 10, y, LIST_W - 8, 20).build());
            y += 23;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.custom"),
                        b -> this.minecraft.setScreen(new CustomGenesScreen(this)))
                .bounds(left + 10, y + 4, LIST_W - 8, 20).build());

        int px = left + LIST_W + 14;
        int pw = W - LIST_W - 24;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.confirm"), b -> {
                    GensNetwork.CHANNEL.sendToServer(new SelectPacket(false, selected.id, new int[GensStat.COUNT]));
                    this.onClose();
                })
                .bounds(px, top + H - 30, pw, 20).build());
    }

    private static double maxOf(ToDoubleFunction<GensLevel> getter) {
        double m = 1.0;
        for (GensLevel l : GensLevel.values()) m = Math.max(m, getter.applyAsDouble(l));
        return m;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int color = accent(selected);

        // Panel
        g.fill(left, top, left + W, top + H, 0xE0101018);
        g.fill(left, top, left + W, top + 3, color);
        g.renderOutline(left, top, W, H, 0xFF303040);
        g.fill(left + LIST_W + 6, top + 30, left + LIST_W + 7, top + H - 10, 0x40FFFFFF);

        g.drawCenteredString(this.font, this.title, left + W / 2, top + 11, 0xFFFFFF);

        // Secili seviyenin seridi
        int sy = top + 34 + selected.ordinal() * 23;
        g.fill(left + 5, sy, left + 8, sy + 20, color);

        super.render(g, mouseX, mouseY, partialTick);

        // Sag panel
        int px = left + LIST_W + 14;
        int pw = W - LIST_W - 24;
        int y = top + 32;

        g.drawString(this.font, Component.translatable(selected.langKey()), px, y, color, true);
        y += 13;

        List<FormattedCharSequence> lines = this.font.split(
                Component.translatable(selected.langKey() + ".desc"), pw);
        for (int i = 0; i < Math.min(lines.size(), 3); i++) {
            g.drawString(this.font, lines.get(i), px, y, 0xFFBBBBBB, false);
            y += 10;
        }

        y = top + 82;
        int labelW = 66;
        int barX = px + labelW;
        int barW = pw - labelW - 34;

        for (Row row : ROWS) {
            double value = row.getter().applyAsDouble(selected);
            double max = maxOf(row.getter());
            int fill = (int) Math.round(barW * Math.min(1.0, value / max));

            g.drawString(this.font, Component.translatable(row.key()), px, y, 0xFFDDDDDD, false);
            g.fill(barX, y + 1, barX + barW, y + 7, 0xFF282838);
            g.fill(barX, y + 1, barX + fill, y + 7, color);
            g.drawString(this.font, String.format(Locale.ROOT, "x%.2f", value),
                    barX + barW + 5, y, 0xFFFFFFFF, false);
            y += 12;
        }

        g.drawCenteredString(this.font, Component.translatable("screen.viltrumite_gens.select_hint"),
                left + LIST_W / 2 + 6, top + H - 12, 0xFF888888);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

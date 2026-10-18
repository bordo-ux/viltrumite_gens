package com.viltrumitegens.client;

import com.viltrumitegens.data.GensLevel;
import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import com.viltrumitegens.network.SelectPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class LevelSelectScreen extends Screen {

    public LevelSelectScreen() {
        super(Component.translatable("screen.viltrumite_gens.title"));
    }

    @Override
    protected void init() {
        int w = 220;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 95;

        for (GensLevel lvl : GensLevel.values()) {
            Button b = Button.builder(Component.translatable(lvl.langKey()), btn -> {
                        GensNetwork.CHANNEL.sendToServer(new SelectPacket(false, lvl.id, new int[GensStat.COUNT]));
                        this.onClose();
                    })
                    .bounds(x, y, w, h)
                    .tooltip(Tooltip.create(Component.literal(describe(lvl))))
                    .build();
            this.addRenderableWidget(b);
            y += 24;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("screen.viltrumite_gens.custom"),
                        btn -> this.minecraft.setScreen(new CustomGenesScreen(this)))
                .bounds(x, y + 8, w, h)
                .build());
    }

    private static String describe(GensLevel l) {
        return String.format(Locale.ROOT,
                "HP x%.2f | DMG x%.2f | DUR x%.2f | SPD x%.2f | REGEN x%.2f%nPower x%.2f | Area x%.2f | Destruction x%.2f",
                l.health, l.damage, l.durability, l.speed, l.regen, l.power, l.area, l.destruction);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 120, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

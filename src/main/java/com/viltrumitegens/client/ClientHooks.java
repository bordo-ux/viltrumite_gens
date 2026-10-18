package com.viltrumitegens.client;

import net.minecraft.client.Minecraft;

/** Sadece istemcide yuklenir (DistExecutor uzerinden cagrilir). */
public final class ClientHooks {
    public static int pool = 100;
    public static int minPerGene = -20;
    public static float percentPerPoint = 5.0f;

    private ClientHooks() {}

    public static void openMenu(int poolValue, int minValue, float percentValue) {
        pool = poolValue;
        minPerGene = minValue;
        percentPerPoint = percentValue;
        Minecraft.getInstance().setScreen(new LevelSelectScreen());
    }
}

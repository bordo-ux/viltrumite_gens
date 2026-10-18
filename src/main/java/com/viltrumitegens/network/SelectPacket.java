package com.viltrumitegens.network;

import com.viltrumitegens.capability.GensCapability;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensLevel;
import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.logic.GensAdvancements;
import com.viltrumitegens.logic.GensStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Istemci -> sunucu: secilen seviye veya ozel genetik puanlari. Sunucu hepsini dogrular. */
public class SelectPacket {
    public final boolean custom;
    public final int levelId;
    public final int[] points;

    public SelectPacket(boolean custom, int levelId, int[] points) {
        this.custom = custom;
        this.levelId = levelId;
        this.points = points;
    }

    public static void encode(SelectPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.custom);
        buf.writeInt(p.levelId);
        for (int i = 0; i < GensStat.COUNT; i++) buf.writeInt(p.points[i]);
    }

    public static SelectPacket decode(FriendlyByteBuf buf) {
        boolean custom = buf.readBoolean();
        int level = buf.readInt();
        int[] pts = new int[GensStat.COUNT];
        for (int i = 0; i < GensStat.COUNT; i++) pts[i] = buf.readInt();
        return new SelectPacket(custom, level, pts);
    }

    public static void handle(SelectPacket p, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            GensCapability.get(sp).ifPresent(d -> {
                boolean mayChange = !d.hasChosen() || GensConfig.ALLOW_RESELECT.get() || sp.hasPermissions(2);
                if (!mayChange) {
                    sp.sendSystemMessage(Component.translatable("message.viltrumite_gens.denied"));
                    return;
                }

                if (p.custom) {
                    int pool = GensConfig.CUSTOM_POINT_POOL.get();
                    int min = GensConfig.CUSTOM_MIN_PER_GENE.get();
                    int sum = 0;
                    float[] pct = new float[GensStat.COUNT];
                    float per = GensConfig.CUSTOM_POINT_PERCENT.get().floatValue();
                    for (int i = 0; i < GensStat.COUNT; i++) {
                        int v = p.points[i];
                        if (v < min || v > pool) {
                            sp.sendSystemMessage(Component.translatable("message.viltrumite_gens.denied"));
                            return;
                        }
                        sum += v;
                        pct[i] = v * per;
                    }
                    if (sum > pool) {
                        sp.sendSystemMessage(Component.translatable("message.viltrumite_gens.denied"));
                        return;
                    }
                    d.chooseCustom(pct);
                } else {
                    d.chooseLevel(GensLevel.byId(p.levelId));
                }

                GensStats.apply(sp, d);
                sp.setHealth(sp.getMaxHealth());
                sp.sendSystemMessage(Component.translatable("message.viltrumite_gens.chosen"));
                GensAdvancements.onChoice(sp, d);
            });
        });
        c.setPacketHandled(true);
    }
}

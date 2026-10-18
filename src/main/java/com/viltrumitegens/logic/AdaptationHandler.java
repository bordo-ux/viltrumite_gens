package com.viltrumitegens.logic;

import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensData;
import com.viltrumitegens.data.GensStat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Yara sonrasi guclenme:
 *   kazanim(%) = katsayi x n^us   (n = tek bir iyilesme doneminde yenilenen yara, yarim kalp = 1)
 * Varsayilan: 0.08 x n^2  ->  yarim kalp %0.08, 1 kalp %0.32, 5 kalp %8, 10 kalp %32.
 * Kazanim, yaranin hangi hasar turunden geldigine gore ilgili istatistige dagitilir.
 */
public final class AdaptationHandler {
    private AdaptationHandler() {}

    /** Hasar turunu istatistik kategorisine cevirir. */
    public static GensStat categorize(DamageSource src) {
        if (src.is(DamageTypeTags.IS_EXPLOSION)) return GensStat.DURABILITY;
        if (src.is(DamageTypeTags.IS_PROJECTILE)) return GensStat.SPEED;
        if (src.getEntity() != null) return GensStat.DAMAGE;
        if (src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypeTags.IS_FALL)) return GensStat.REGEN;
        return GensStat.HEALTH;
    }

    /** Cevre veya kendi verdigi hasar mi? */
    public static boolean isEnvironmental(DamageSource src, ServerPlayer victim) {
        return src.getEntity() == null || src.getEntity() == victim;
    }

    public static void recordDamage(ServerPlayer player, GensData data, DamageSource src, float amount) {
        if (!data.hasChosen()) return;
        double factor = isEnvironmental(src, player) ? GensConfig.ENVIRONMENT_DAMAGE_FACTOR.get() : 1.0;
        data.recordWound(categorize(src), (float) (amount * factor));
    }

    /** Her tick cagrilir (sunucu tarafi). */
    public static void tick(ServerPlayer player, GensData data) {
        if (!data.hasChosen()) return;

        data.tickCooldown();
        float deficit = player.getMaxHealth() - player.getHealth();
        data.trackDeficit(deficit);

        // Tamamen iyilesti -> kazanimi ver
        if (deficit <= 0.01f && data.peakDeficit() > 0f && data.cooldown() <= 0) {
            grant(player, data);
        }

        // Ekstra iyilesme hizi
        if (player.tickCount % 20 == 0 && deficit > 0.01f && player.isAlive()) {
            double regen = data.multiplier(GensStat.REGEN);
            if (regen > 1.0) {
                player.heal((float) ((regen - 1.0) * 0.25));
            }
        }
    }

    private static void grant(ServerPlayer player, GensData data) {
        float n = Math.min(data.totalWound(), data.peakDeficit());
        if (n <= 0f) {
            data.resetWound();
            return;
        }

        double gain = GensConfig.ADAPT_COEFFICIENT.get() * Math.pow(n, GensConfig.ADAPT_EXPONENT.get());
        boolean cap = GensConfig.ADAPT_CAP_ENABLED.get();
        float capPct = GensConfig.ADAPT_CAP_PERCENT.get().floatValue();

        StringBuilder msg = new StringBuilder();
        for (GensStat s : GensStat.values()) {
            float share = data.woundShare(s);
            if (share <= 0f) continue;
            float add = (float) (gain * share);
            data.addAdaptation(s, add, cap, capPct);
            if (msg.length() > 0) msg.append(", ");
            msg.append(String.format("%s +%.2f%%",
                    Component.translatable(s.langKey()).getString(), add));
        }

        data.resetWound();
        data.setCooldown(GensConfig.GAIN_COOLDOWN_TICKS.get());
        GensStats.apply(player, data);
        player.displayClientMessage(
                Component.translatable("message.viltrumite_gens.adapted", msg.toString()), true);
    }
}

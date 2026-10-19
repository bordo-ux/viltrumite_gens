package com.viltrumitegens.logic;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sarjli yumruk: ust seviyelerde sol tiki basili tut -> sarj dolar -> birak -> sok dalgasi.
 * Sol tiki hizlica tiklamak normal vurustur, sok dalgasi yapmaz.
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID)
public final class Charge {
    private static final Map<UUID, Boolean> HOLD = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> TICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_FIRE = new ConcurrentHashMap<>();

    private Charge() {}

    public static void setHolding(ServerPlayer sp, boolean holding) {
        if (holding) HOLD.put(sp.getUUID(), Boolean.TRUE);
        else HOLD.remove(sp.getUUID());
    }

    /** Bu oyuncunun seviyesi sarjli yumruga yetiyor mu? */
    public static boolean qualifies(GensData d) {
        return d.hasChosen() && GensConfig.SHOCK_ENABLED.get()
                && (d.areaMultiplier() >= GensConfig.SHOCK_MIN_AREA.get()
                || d.destructionMultiplier() >= GensConfig.SHOCK_BLOCK_THRESHOLD.get());
    }

    public static boolean isCharging(Player p) {
        return TICKS.getOrDefault(p.getUUID(), 0) >= 3;
    }

    private static void clear(UUID id) {
        HOLD.remove(id);
        TICKS.remove(id);
        LAST_FIRE.remove(id);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity().getUUID());
    }

    /** Sarj sirasinda blok kirma ilerlemesini durdur (yoksa sarj ederken yerdeki blogu kirarsin). */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (isCharging(event.getEntity())) {
            event.setNewSpeed(0.0F);
        }
    }

    /** Her tick (sunucu). */
    public static void tick(ServerPlayer sp, GensData d) {
        UUID id = sp.getUUID();
        boolean handOk = !GensConfig.CHARGE_EMPTY_HAND.get() || sp.getMainHandItem().isEmpty();
        boolean hold = HOLD.containsKey(id) && qualifies(d) && handOk && sp.isAlive() && !sp.isSpectator();
        int t = TICKS.getOrDefault(id, 0);
        int full = GensConfig.CHARGE_FULL_TICKS.get();
        int min = GensConfig.CHARGE_MIN_TICKS.get();

        if (hold) {
            int next = Math.min(full, t + 1);
            TICKS.put(id, next);
            float frac = next / (float) full;

            if (next >= 4 && next % 4 == 0) {
                ServerLevel level = sp.serverLevel();
                level.sendParticles(ParticleTypes.CRIT, sp.getX(), sp.getY() + 1.0, sp.getZ(),
                        2 + (int) (frac * 8), 0.45, 0.6, 0.45, 0.08);

                int bars = Math.round(frac * 10f);
                String bar = "█".repeat(bars) + "░".repeat(10 - bars) + " " + Math.round(frac * 100f) + "%";
                sp.displayClientMessage(Component.translatable("message.viltrumite_gens.charge", bar), true);
            }
            if (next == full && t < full) {
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.6f);
            }
        } else if (t > 0) {
            TICKS.remove(id);
            if (sp.isAlive() && t >= min) {
                long now = sp.level().getGameTime();
                Long last = LAST_FIRE.get(id);
                if (last == null || now - last >= GensConfig.SHOCK_COOLDOWN_TICKS.get()) {
                    LAST_FIRE.put(id, now);
                    fire(sp, d, Math.min(1.0f, t / (float) full));
                }
            }
        }
    }

    /** Bakilan noktayi bul (once canli, yoksa blok, yoksa menzil sonu) ve sok dalgasini orada patlat. */
    private static void fire(ServerPlayer sp, GensData d, float frac) {
        ServerLevel level = sp.serverLevel();
        double reach = GensConfig.CHARGE_REACH.get();
        Vec3 eye = sp.getEyePosition();
        Vec3 end = eye.add(sp.getLookAngle().scale(reach));

        BlockHitResult bhr = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sp));
        Vec3 target = bhr.getType() == HitResult.Type.MISS ? end : bhr.getLocation();

        AABB box = sp.getBoundingBox().expandTowards(sp.getLookAngle().scale(reach)).inflate(1.0);
        EntityHitResult ehr = ProjectileUtil.getEntityHitResult(level, sp, eye, end, box,
                (Entity e) -> !e.isSpectator() && e.isPickable());
        if (ehr != null && eye.distanceToSqr(ehr.getLocation()) <= eye.distanceToSqr(target)) {
            target = ehr.getLocation();
        }

        Shockwave.burst(sp, d, target, frac);
    }
}

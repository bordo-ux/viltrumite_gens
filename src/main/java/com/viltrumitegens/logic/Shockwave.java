package com.viltrumitegens.logic;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.capability.GensCapability;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Seviyeyle buyuyen tahrip alani.
 *
 * 1) Sok dalgasi: guclu bir vurusta hedefin cevresine alan hasari ve (yuksek seviyede) blok kirma.
 *    Canlilara etki yaricapi = entityRadius x seviyenin ALAN carpani.
 *    Blok kirma yaricapi    = blockRadius  x seviyenin TAHRIP carpani.
 * 2) Patlama buyutme: oyuncunun yol actigi patlamalar (ana mod patlama kullaniyorsa) ayni carpanlarla buyur.
 *
 * Ana modun kendi ozel yetenekleri patlama kullanmiyorsa onlara buradan mudahale edilemez;
 * onlar icin ana modun kodu gerekir.
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID)
public class Shockwave {
    private static boolean active = false;
    private static final Map<UUID, Long> LAST = new HashMap<>();

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST.remove(event.getEntity().getUUID());
    }

    // ------------------------------------------------------------ 1) sok dalgasi

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (active) return;
        DamageSource src = event.getSource();
        if (!(src.getEntity() instanceof ServerPlayer sp) || src.getDirectEntity() != sp) return;
        if (!GensConfig.SHOCK_ENABLED.get()) return;
        if (event.getAmount() < GensConfig.SHOCK_MIN_DAMAGE.get()) return;

        GensData d = GensCapability.get(sp).orElse(null);
        if (d == null || !d.hasChosen()) return;

        boolean entityPhase = d.areaMultiplier() >= GensConfig.SHOCK_MIN_AREA.get();
        boolean blockPhase = d.destructionMultiplier() >= GensConfig.SHOCK_BLOCK_THRESHOLD.get();
        if (!entityPhase && !blockPhase) return;

        ServerLevel level = sp.serverLevel();
        long now = level.getGameTime();
        Long last = LAST.get(sp.getUUID());
        if (last != null && now - last < GensConfig.SHOCK_COOLDOWN_TICKS.get()) return;
        LAST.put(sp.getUUID(), now);

        LivingEntity victim = event.getEntity();
        Vec3 c = victim.position().add(0, victim.getBbHeight() * 0.5, 0);
        double maxR = GensConfig.SHOCK_MAX_RADIUS.get();

        double entityR = entityPhase ? Math.min(maxR, GensConfig.SHOCK_ENTITY_RADIUS.get() * d.areaMultiplier()) : 0;
        double blockR = blockPhase ? Math.min(maxR, GensConfig.SHOCK_BLOCK_RADIUS.get() * d.destructionMultiplier()) : 0;

        active = true;
        try {
            if (entityR > 0) hurtNearby(level, sp, victim, c, entityR, event.getAmount());
            if (blockR > 0) breakBlocks(level, sp, c, blockR, d.destructionMultiplier());
        } finally {
            active = false;
        }

        double r = Math.max(entityR, blockR);
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z,
                (int) Math.min(40, 5 + r * 2), r * 0.3, r * 0.15, r * 0.3, 0.02);
        if (r >= 6) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                (float) Math.min(4.0, 1.0 + r * 0.15), 0.9f);
    }

    private static void hurtNearby(ServerLevel level, ServerPlayer sp, LivingEntity victim,
                                   Vec3 c, double r, float hit) {
        AABB box = new AABB(c, c).inflate(r);
        float base = hit * GensConfig.SHOCK_DAMAGE_FRACTION.get().floatValue();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == sp || e == victim || !e.isAlive()) continue;
            if (e instanceof Player && !GensConfig.SHOCK_HURT_PLAYERS.get()) continue;
            if (e instanceof TamableAnimal t && t.isOwnedBy(sp)) continue;

            double dist = e.position().distanceTo(c);
            if (dist > r) continue;
            double falloff = 1.0 - dist / r;
            float dmg = (float) (base * falloff);
            if (dmg <= 0.5f) continue;

            e.hurt(level.damageSources().playerAttack(sp), dmg);
            Vec3 push = e.position().subtract(c).normalize().scale(0.2 + 0.6 * falloff);
            e.push(push.x, 0.25, push.z);
            e.hurtMarked = true;
        }
    }

    private static void breakBlocks(ServerLevel level, ServerPlayer sp, Vec3 c, double r, double destruction) {
        double limit = GensConfig.SHOCK_HARDNESS_PER_LEVEL.get() * destruction;
        int max = GensConfig.SHOCK_MAX_BLOCKS.get();
        boolean drops = GensConfig.SHOCK_DROPS.get();
        BlockPos origin = BlockPos.containing(c);
        BlockPos self = sp.blockPosition();
        int ri = (int) Math.ceil(r);
        int minY = sp.getBlockY();
        int count = 0;

        outer:
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (pos.getY() < minY) continue;                       // oyuncunun ayagi altini kirma
                    if (pos.equals(self) || pos.equals(self.above())) continue;

                    BlockState st = level.getBlockState(pos);
                    if (st.isAir() || !st.getFluidState().isEmpty()) continue;
                    float h = st.getDestroySpeed(level, pos);
                    if (h < 0 || h > limit) continue;                      // bedrock vb. kirilmaz
                    if (!level.mayInteract(sp, pos)) continue;             // spawn korumasi vb.

                    level.destroyBlock(pos, drops, sp);
                    if (++count >= max) break outer;
                }
            }
        }
    }

    // ------------------------------------------------------------ 2) patlama buyutme

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (!GensConfig.SCALE_EXPLOSIONS.get()) return;
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Explosion ex = event.getExplosion();
        if (!(ex.getIndirectSourceEntity() instanceof ServerPlayer sp)) return;
        GensData d = GensCapability.get(sp).orElse(null);
        if (d == null || !d.hasChosen()) return;

        Vec3 c = ex.getPosition();
        double maxR = GensConfig.SHOCK_MAX_RADIUS.get();

        // Orijinal patlama yaricapini etkilenen bloklardan tahmin et
        List<BlockPos> blocks = event.getAffectedBlocks();
        double r0 = 2.0;
        for (BlockPos p : blocks) r0 = Math.max(r0, Math.sqrt(p.distToCenterSqr(c)));

        double dest = d.destructionMultiplier();
        if (dest > 1.0) {
            double r = Math.min(maxR, r0 * dest);
            double limit = GensConfig.SHOCK_HARDNESS_PER_LEVEL.get() * dest;
            int max = GensConfig.SHOCK_MAX_BLOCKS.get();
            Set<BlockPos> existing = new HashSet<>(blocks);
            BlockPos origin = BlockPos.containing(c);
            int ri = (int) Math.ceil(r);
            int added = 0;

            outer:
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dy = -ri; dy <= ri; dy++) {
                    for (int dz = -ri; dz <= ri; dz++) {
                        if (dx * dx + dy * dy + dz * dz > r * r) continue;
                        BlockPos pos = origin.offset(dx, dy, dz);
                        if (existing.contains(pos)) continue;
                        BlockState st = level.getBlockState(pos);
                        if (st.isAir() || !st.getFluidState().isEmpty()) continue;
                        float h = st.getDestroySpeed(level, pos);
                        if (h < 0 || h > limit) continue;
                        if (!level.mayInteract(sp, pos)) continue;
                        blocks.add(pos);
                        if (++added >= max) break outer;
                    }
                }
            }
        }

        double area = d.areaMultiplier();
        if (area > 1.0) {
            double r = Math.min(maxR, r0 * area);
            List<Entity> affected = event.getAffectedEntities();
            for (Entity e : level.getEntities((Entity) null, new AABB(c, c).inflate(r))) {
                if (e == sp || e.ignoreExplosion() || affected.contains(e)) continue;
                if (e.position().distanceTo(c) <= r) affected.add(e);
            }
        }
    }
}

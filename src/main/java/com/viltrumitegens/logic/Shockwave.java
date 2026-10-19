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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seviyeyle buyuyen tahrip alani.
 *
 * 1) burst(): sarjli yumrugun sok dalgasi ({@link Charge} tetikler).
 *    Canlilara etki yaricapi = entityRadius x ALAN carpani x sarj,
 *    blok kirma yaricapi    = blockRadius  x TAHRIP carpani x sarj.
 *    Bloklar yukari VE asagi (kure seklinde) kirilir.
 * 2) onDetonate(): oyuncunun yol actigi patlamalar (ana mod patlama kullaniyorsa) ayni carpanlarla buyur.
 *
 * Ana modun kendi ozel yetenekleri patlama kullanmiyorsa onlara buradan mudahale edilemez;
 * onlar icin ana modun kodu gerekir.
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID)
public class Shockwave {

    // ------------------------------------------------------------ 1) sarjli sok dalgasi

    /** c noktasinda, sarj orani frac (0-1) ile sok dalgasi yap. */
    public static void burst(ServerPlayer sp, GensData d, Vec3 c, float frac) {
        ServerLevel level = sp.serverLevel();
        double maxR = GensConfig.SHOCK_MAX_RADIUS.get();
        double scale = 0.4 + 0.6 * frac;

        double entityR = 0;
        if (d.areaMultiplier() >= GensConfig.SHOCK_MIN_AREA.get()) {
            entityR = GensConfig.SHOCK_ENTITY_RADIUS.get() * d.areaMultiplier() * scale;
        }
        entityR = Math.min(maxR, Math.max(1.5, entityR));   // hedefe her zaman vursun

        double blockR = 0;
        if (d.destructionMultiplier() >= GensConfig.SHOCK_BLOCK_THRESHOLD.get()) {
            blockR = Math.min(maxR, GensConfig.SHOCK_BLOCK_RADIUS.get() * d.destructionMultiplier() * scale);
        }

        double attack = sp.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float hit = (float) ((attack + 3.0 * d.powerMultiplier()) * (0.5 + 1.5 * frac)
                * GensConfig.CHARGE_DAMAGE_SCALE.get());

        hurtNearby(level, sp, c, entityR, hit);
        if (blockR > 0) breakBlocks(level, sp, c, blockR, d.destructionMultiplier());

        double r = Math.max(entityR, blockR);
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z,
                (int) Math.min(40, 5 + r * 2), r * 0.3, r * 0.15, r * 0.3, 0.02);
        if (r >= 6) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                (float) Math.min(4.0, 1.0 + r * 0.15), 0.9f);
    }

    private static void hurtNearby(ServerLevel level, ServerPlayer sp, Vec3 c, double r, float hit) {
        AABB box = new AABB(c, c).inflate(r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == sp || !e.isAlive()) continue;
            if (e instanceof Player && !GensConfig.SHOCK_HURT_PLAYERS.get()) continue;
            if (e instanceof TamableAnimal t && t.isOwnedBy(sp)) continue;

            double dist = e.position().add(0, e.getBbHeight() * 0.5, 0).distanceTo(c);
            if (dist > r) continue;
            double falloff = 1.0 - 0.75 * (dist / r);        // merkezde tam, kenarda %25
            float dmg = (float) (hit * falloff);
            if (dmg <= 0.5f) continue;

            e.hurt(level.damageSources().playerAttack(sp), dmg);
            Vec3 push = e.position().subtract(c).normalize().scale(0.3 + 0.9 * falloff);
            e.push(push.x, 0.3, push.z);
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
        int count = 0;

        outer:
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -ri; dy <= ri; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);

                    // Oyuncunun kendi bastigi zemini ve vucudunu kirma (boslukta dusmesin)
                    if (pos.getY() <= self.getY()
                            && Math.abs(pos.getX() - self.getX()) <= 1
                            && Math.abs(pos.getZ() - self.getZ()) <= 1) continue;
                    if (pos.equals(self.above())) continue;

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

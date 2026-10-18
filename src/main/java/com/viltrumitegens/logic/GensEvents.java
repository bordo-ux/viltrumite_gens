package com.viltrumitegens.logic;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.capability.GensCapability;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID)
public class GensEvents {

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(ViltrumiteGens.MODID, "gens"),
                    new GensCapability.Provider());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        GensCapability.get(sp).ifPresent(d -> {
            GensStats.apply(sp, d);
            if (!d.hasChosen() && GensConfig.AUTO_OPEN_MENU.get() && ViltrumiteBridge.shouldOfferMenu(sp)) {
                GensNetwork.openMenu(sp);
            }
        });
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        event.getOriginal().reviveCaps();
        GensCapability.get(event.getOriginal()).ifPresent(old ->
                GensCapability.get(event.getEntity()).ifPresent(neu -> {
                    neu.copyFrom(old);
                    if (event.isWasDeath()) {
                        neu.applyDeathLoss(GensConfig.DEATH_LOSS.get());
                    }
                }));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            GensCapability.get(sp).ifPresent(d -> GensStats.apply(sp, d));
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            GensCapability.get(sp).ifPresent(d -> GensStats.apply(sp, d));
        }
    }

    /** Dayaniklilik: gelen hasari azaltir (yumusak formul, dokunulmazlik yok). */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        GensCapability.get(sp).ifPresent(d -> {
            if (!d.hasChosen()) return;
            double m = d.multiplier(GensStat.DURABILITY);
            if (m > 1.0) {
                event.setAmount((float) (event.getAmount() / (1.0 + (m - 1.0) * 0.5)));
            }
        });
    }

    /** Alinan nihai hasari adaptasyon icin kaydeder. */
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        GensCapability.get(sp).ifPresent(d -> {
            AdaptationHandler.recordDamage(sp, d, event.getSource(), event.getAmount());
            // "Think, Mark, think!" - 7+ kalplik dususten sag kalk
            if (d.hasChosen() && event.getSource().is(DamageTypeTags.IS_FALL)
                    && event.getAmount() >= 14f && sp.getHealth() - event.getAmount() > 0f) {
                GensAdvancements.grant(sp, "think_mark_think");
            }
        });
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player instanceof ServerPlayer sp) {
            GensCapability.get(sp).ifPresent(d -> AdaptationHandler.tick(sp, d));
        }
    }
}

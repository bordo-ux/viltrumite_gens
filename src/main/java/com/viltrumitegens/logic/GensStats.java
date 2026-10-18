package com.viltrumitegens.logic;

import com.viltrumitegens.data.GensData;
import com.viltrumitegens.data.GensStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** Seviye + ozel genetik + adaptasyon carpanlarini oyuncunun attribute'larina yazar. */
public final class GensStats {
    private static final UUID HEALTH_ID = UUID.fromString("5e1a7c10-0001-4a11-9c01-a1b2c3d4e501");
    private static final UUID DAMAGE_ID = UUID.fromString("5e1a7c10-0002-4a11-9c01-a1b2c3d4e502");
    private static final UUID SPEED_ID = UUID.fromString("5e1a7c10-0003-4a11-9c01-a1b2c3d4e503");
    private static final UUID KB_ID = UUID.fromString("5e1a7c10-0004-4a11-9c01-a1b2c3d4e504");

    private GensStats() {}

    public static void apply(ServerPlayer player, GensData data) {
        boolean on = data.hasChosen();

        // Can: taban 20 uzerine ekleme
        set(player, Attributes.MAX_HEALTH, HEALTH_ID, "gens_health",
                on ? (data.multiplier(GensStat.HEALTH) - 1.0) * 20.0 : 0.0,
                AttributeModifier.Operation.ADDITION);

        // Hasar ve hiz: tabanin carpani
        set(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, "gens_damage",
                on ? data.multiplier(GensStat.DAMAGE) - 1.0 : 0.0,
                AttributeModifier.Operation.MULTIPLY_BASE);

        set(player, Attributes.MOVEMENT_SPEED, SPEED_ID, "gens_speed",
                on ? data.multiplier(GensStat.SPEED) - 1.0 : 0.0,
                AttributeModifier.Operation.MULTIPLY_BASE);

        // Dayaniklilik: geri itilme direnci (hasar azaltma ayrica GensEvents'te)
        double kb = on ? Math.min(1.0, (data.multiplier(GensStat.DURABILITY) - 1.0) * 0.1) : 0.0;
        set(player, Attributes.KNOCKBACK_RESISTANCE, KB_ID, "gens_durability", kb,
                AttributeModifier.Operation.ADDITION);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(ServerPlayer p, Attribute attr, UUID id, String name,
                            double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (Math.abs(amount) > 1.0E-6) {
            inst.addPermanentModifier(new AttributeModifier(id, name, amount, op));
        }
    }
}

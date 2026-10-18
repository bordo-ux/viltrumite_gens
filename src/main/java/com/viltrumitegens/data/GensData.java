package com.viltrumitegens.data;

import net.minecraft.nbt.CompoundTag;

/** Oyuncuya bagli kalici veri: seviye, ozel genetik ve adaptasyon. */
public class GensData {
    private boolean chosen;
    private GensLevel level = GensLevel.SOLDIER;
    private boolean custom;
    private final float[] customPct = new float[GensStat.COUNT];
    private final float[] adaptPct = new float[GensStat.COUNT];

    // Anlik (kaydedilmeyen degil, kaydedilen) yara takibi
    private final float[] woundBy = new float[GensStat.COUNT];
    private float peakDeficit;
    private int cooldown;

    // ---------- okuma ----------
    public boolean hasChosen() { return chosen; }
    public GensLevel level() { return level; }
    public boolean isCustom() { return custom; }
    public float adaptation(GensStat s) { return adaptPct[s.ordinal()]; }
    public float customBonus(GensStat s) { return customPct[s.ordinal()]; }

    /** Nihai carpan = seviye carpani x (1 + (ozel% + adaptasyon%) / 100). */
    public double multiplier(GensStat s) {
        double bonus = (customPct[s.ordinal()] + adaptPct[s.ordinal()]) / 100.0;
        return Math.max(0.1, level.get(s) * (1.0 + bonus));
    }

    /** Ana modun yetenekleri icin: guc, etki alani, tahrip carpanlari. */
    public double powerMultiplier() { return level.power * (1.0 + adaptPct[GensStat.DAMAGE.ordinal()] / 100.0); }
    public double areaMultiplier() { return level.area; }
    public double destructionMultiplier() { return level.destruction; }

    // ---------- secim ----------
    public void chooseLevel(GensLevel lvl) {
        this.chosen = true;
        this.custom = false;
        this.level = lvl;
        java.util.Arrays.fill(customPct, 0f);
    }

    public void chooseCustom(float[] pct) {
        this.chosen = true;
        this.custom = true;
        this.level = GensLevel.SOLDIER;
        for (int i = 0; i < GensStat.COUNT; i++) customPct[i] = pct[i];
    }

    public void resetChoice() {
        this.chosen = false;
        this.custom = false;
        this.level = GensLevel.SOLDIER;
        java.util.Arrays.fill(customPct, 0f);
        resetWound();
    }

    // ---------- adaptasyon ----------
    public void recordWound(GensStat cat, float amount) {
        if (amount > 0) woundBy[cat.ordinal()] += amount;
    }

    public float totalWound() {
        float t = 0;
        for (float f : woundBy) t += f;
        return t;
    }

    public float woundShare(GensStat s) {
        float t = totalWound();
        return t <= 0 ? 0 : woundBy[s.ordinal()] / t;
    }

    public void trackDeficit(float deficit) {
        if (deficit > peakDeficit) peakDeficit = deficit;
    }

    public float peakDeficit() { return peakDeficit; }

    public void resetWound() {
        java.util.Arrays.fill(woundBy, 0f);
        peakDeficit = 0f;
    }

    public void addAdaptation(GensStat s, float pct, boolean capEnabled, float capPct) {
        float v = adaptPct[s.ordinal()] + pct;
        if (capEnabled) v = Math.min(v, capPct);
        adaptPct[s.ordinal()] = v;
    }

    public int cooldown() { return cooldown; }
    public void setCooldown(int ticks) { cooldown = ticks; }
    public void tickCooldown() { if (cooldown > 0) cooldown--; }

    /** Olumde adaptasyon bonusunun bir kismi kaybolur. */
    public void applyDeathLoss(double lossFraction) {
        float keep = (float) (1.0 - lossFraction);
        for (int i = 0; i < GensStat.COUNT; i++) adaptPct[i] *= keep;
        resetWound();
    }

    public void copyFrom(GensData o) {
        this.chosen = o.chosen;
        this.level = o.level;
        this.custom = o.custom;
        System.arraycopy(o.customPct, 0, customPct, 0, GensStat.COUNT);
        System.arraycopy(o.adaptPct, 0, adaptPct, 0, GensStat.COUNT);
        System.arraycopy(o.woundBy, 0, woundBy, 0, GensStat.COUNT);
        this.peakDeficit = o.peakDeficit;
        this.cooldown = o.cooldown;
    }

    // ---------- NBT ----------
    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("chosen", chosen);
        t.putInt("level", level.id);
        t.putBoolean("custom", custom);
        for (GensStat s : GensStat.values()) {
            t.putFloat("custom_" + s.name(), customPct[s.ordinal()]);
            t.putFloat("adapt_" + s.name(), adaptPct[s.ordinal()]);
            t.putFloat("wound_" + s.name(), woundBy[s.ordinal()]);
        }
        t.putFloat("peak", peakDeficit);
        t.putInt("cooldown", cooldown);
        return t;
    }

    public void load(CompoundTag t) {
        chosen = t.getBoolean("chosen");
        level = GensLevel.byId(t.getInt("level"));
        custom = t.getBoolean("custom");
        for (GensStat s : GensStat.values()) {
            customPct[s.ordinal()] = t.getFloat("custom_" + s.name());
            adaptPct[s.ordinal()] = t.getFloat("adapt_" + s.name());
            woundBy[s.ordinal()] = t.getFloat("wound_" + s.name());
        }
        peakDeficit = t.getFloat("peak");
        cooldown = t.getInt("cooldown");
    }
}

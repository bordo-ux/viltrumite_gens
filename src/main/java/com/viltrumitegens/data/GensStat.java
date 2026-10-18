package com.viltrumitegens.data;

public enum GensStat {
    HEALTH, DAMAGE, DURABILITY, SPEED, REGEN;

    public static final int COUNT = values().length;

    public String langKey() {
        return "stat.viltrumite_gens." + name().toLowerCase();
    }
}

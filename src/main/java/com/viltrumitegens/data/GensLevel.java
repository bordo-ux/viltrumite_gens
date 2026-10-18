package com.viltrumitegens.data;

/**
 * 6 seviye. Her deger bir carpandir (1.0 = normal Viltrumlu askerin kendi gucu).
 * Her seviye bir oncekinin ustune koyar.
 *
 * power / area / destruction: ana modun yeteneklerinin gucu, etki alani ve tahrip gucu
 * icin okunacak carpanlar (GensData.powerMultiplier() vb.).
 */
public enum GensLevel {
    //            id  health dmg   dur   speed regen power area  destruction
    SOLDIER(1,   1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, "soldier"),
    MARK(2,      1.50, 1.50, 1.50, 1.15, 1.50, 1.00, 1.00, 1.00, "mark"),
    CONQUEST(3,  2.00, 2.25, 2.00, 1.30, 2.00, 1.50, 1.50, 1.00, "conquest"),
    NOLAN(4,     2.75, 3.25, 2.75, 1.50, 3.00, 2.25, 2.25, 1.50, "nolan"),
    THRAGG(5,    3.75, 4.50, 3.75, 1.75, 4.50, 3.50, 3.50, 2.50, "thragg"),
    ARGALL(6,    5.00, 6.50, 5.00, 2.25, 6.00, 5.00, 8.00, 6.00, "argall");

    public final int id;
    public final double health, damage, durability, speed, regen;
    public final double power, area, destruction;
    private final String key;

    GensLevel(int id, double health, double damage, double durability, double speed, double regen,
              double power, double area, double destruction, String key) {
        this.id = id;
        this.health = health;
        this.damage = damage;
        this.durability = durability;
        this.speed = speed;
        this.regen = regen;
        this.power = power;
        this.area = area;
        this.destruction = destruction;
        this.key = key;
    }

    public double get(GensStat stat) {
        return switch (stat) {
            case HEALTH -> health;
            case DAMAGE -> damage;
            case DURABILITY -> durability;
            case SPEED -> speed;
            case REGEN -> regen;
        };
    }

    public String langKey() {
        return "level.viltrumite_gens." + key;
    }

    public static GensLevel byId(int id) {
        for (GensLevel l : values()) {
            if (l.id == id) return l;
        }
        return SOLDIER;
    }
}

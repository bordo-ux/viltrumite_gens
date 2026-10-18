package com.viltrumitegens.network;

import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.config.GensConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class GensNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ViltrumiteGens.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private GensNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenMenuPacket.class,
                OpenMenuPacket::encode, OpenMenuPacket::decode, OpenMenuPacket::handle);
        CHANNEL.registerMessage(id++, SelectPacket.class,
                SelectPacket::encode, SelectPacket::decode, SelectPacket::handle);
    }

    /** Oyuncuya secim menusunu actirir (ayarlar pakete konur, istemci config'ine bagli kalmaz). */
    public static void openMenu(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenMenuPacket(
                        GensConfig.CUSTOM_POINT_POOL.get(),
                        GensConfig.CUSTOM_MIN_PER_GENE.get(),
                        GensConfig.CUSTOM_POINT_PERCENT.get().floatValue()));
    }
}

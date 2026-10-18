package com.viltrumitegens.logic;

import com.mojang.brigadier.CommandDispatcher;
import com.viltrumitegens.ViltrumiteGens;
import com.viltrumitegens.capability.GensCapability;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.data.GensData;
import com.viltrumitegens.data.GensStat;
import com.viltrumitegens.network.GensNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

/**
 * /gens menu   -> secim menusunu ac (secim yapmadiysan, veya allowReselect/op ise)
 * /gens info   -> seviye, carpanlar ve adaptasyon bonuslari
 * /gens reset  -> (op) secimi sifirla
 */
@Mod.EventBusSubscriber(modid = ViltrumiteGens.MODID)
public class GensCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("gens")
                .then(Commands.literal("menu").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GensData data = GensCapability.get(p).orElse(null);
                    if (data == null) return 0;
                    boolean allowed = !data.hasChosen() || GensConfig.ALLOW_RESELECT.get() || p.hasPermissions(2);
                    if (!allowed) {
                        ctx.getSource().sendFailure(Component.translatable("message.viltrumite_gens.denied"));
                        return 0;
                    }
                    GensNetwork.openMenu(p);
                    return 1;
                }))
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GensData data = GensCapability.get(p).orElse(null);
                    if (data == null) return 0;
                    StringBuilder sb = new StringBuilder();
                    sb.append(data.isCustom() ? "Custom" : Component.translatable(data.level().langKey()).getString());
                    for (GensStat s : GensStat.values()) {
                        sb.append(String.format(Locale.ROOT, "%n%s: x%.3f (adapt +%.2f%%)",
                                Component.translatable(s.langKey()).getString(),
                                data.multiplier(s), data.adaptation(s)));
                    }
                    sb.append(String.format(Locale.ROOT, "%nPower x%.2f | Area x%.2f | Destruction x%.2f",
                            data.powerMultiplier(), data.areaMultiplier(), data.destructionMultiplier()));
                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                    return 1;
                }))
                .then(Commands.literal("reset").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GensCapability.get(p).ifPresent(data -> {
                        data.resetChoice();
                        GensStats.apply(p, data);
                    });
                    ctx.getSource().sendSuccess(
                            () -> Component.translatable("message.viltrumite_gens.reset"), false);
                    return 1;
                })));
    }
}

package de.saschat.scatterspawn;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.saschat.scatterspawn.api.ScattererProviderEntrypoint;
import de.saschat.scatterspawn.config.ScatterSpawnConfig;
import de.saschat.scatterspawn.logic.CircleScatterer;
import de.saschat.scatterspawn.logic.DefaultScatterer;
import de.saschat.scatterspawn.logic.Scatterer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ScatterSpawn implements DedicatedServerModInitializer, ScattererProviderEntrypoint {
    public static final String MOD_ID = "scatterspawn";
    public static ScatterSpawn INSTANCE;

    private final Map<ResourceLocation, Supplier<Scatterer>> SCATTERER_REGISTRY = new HashMap();
    private ResourceLocation CURRENT_SCATTERER;
    private Scatterer CURRENT_INSTANCE;


    private Set<UUID> RESPAWNING_SET = Collections.synchronizedSet(new HashSet<>());

    public void registerRespawn(UUID player) {
        RESPAWNING_SET.add(player);
    }
    public boolean isRespawning(UUID player) {
        return RESPAWNING_SET.contains(player);
    }
    public void unregisterRespawn(UUID player) {
        RESPAWNING_SET.remove(player);
    }
    public Scatterer.Output getSpawnLocation(ServerPlayer serverPlayerMixin) {
        Scatterer.Output output = CURRENT_INSTANCE.scatter(serverPlayerMixin, ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs(), ScatterSpawnConfig.INSTANCE.getScattererConfig());
        ScatterSpawnConfig.INSTANCE.dirty();
        return output;
    }

    private Scatterer getScatterer() {
        if (CURRENT_INSTANCE == null || CURRENT_SCATTERER == null) {
            CURRENT_INSTANCE = new DefaultScatterer();
            CURRENT_SCATTERER = DefaultScatterer.ID;
        }
        return CURRENT_INSTANCE;
    }

    private boolean setScatterer(ResourceLocation location) {
        Supplier<Scatterer> scatterer;
        if ((scatterer = SCATTERER_REGISTRY.get(location)) != null) {
            try {
                CURRENT_INSTANCE = scatterer.get();
                CURRENT_SCATTERER = location;

                ScatterSpawnConfig.INSTANCE.setScatterer(CURRENT_SCATTERER);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void register(Map<ResourceLocation, Supplier<Scatterer>> scattererRegistry) {
        scattererRegistry.put(DefaultScatterer.ID, DefaultScatterer::new);
        scattererRegistry.put(CircleScatterer.ID, CircleScatterer::new);
    }

    @Override
    public void onInitializeServer() {
        INSTANCE = this;
        ScatterSpawnConfig.INSTANCE.load();
        for (ScattererProviderEntrypoint entrypoint : FabricLoader.getInstance().getEntrypoints(MOD_ID + ":register", ScattererProviderEntrypoint.class))
            entrypoint.register(SCATTERER_REGISTRY);
        setScatterer(ScatterSpawnConfig.INSTANCE.getScatterer());


        Gson GSON = new GsonBuilder().setPrettyPrinting().create();

        // @todo: fix this caffeine fueled rampage and make it not be ugly as all hell
        CommandRegistrationCallback.EVENT.register((dispatch, registry, environment) -> {
            dispatch.register(
                literal("scatterspawn").requires(source -> source.hasPermission(4))
                    .then(literal("scatterers").then(
                        literal("list").executes(ctx -> {
                            // list command
                            ctx.getSource().sendSuccess(() -> Component.literal("Available scatterers:"), false);
                            for (ResourceLocation resourceLocation : SCATTERER_REGISTRY.keySet()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("  " + resourceLocation.toString()), false);
                            }
                            return 0;
                        })).then(literal("set").then(argument("name", ResourceLocationArgument.id())
                        .suggests((ctx, abc) -> SharedSuggestionProvider.suggest(SCATTERER_REGISTRY.keySet().stream().map(ResourceLocation::toString).toList(), abc))
                        .executes(ctx -> {
                            if(setScatterer(ctx.getArgument("name", ResourceLocation.class))) {
                                ctx.getSource().sendSuccess(() -> Component.literal("Success!"), false);
                            } else {
                                ctx.getSource().sendFailure(Component.literal("Failed to set scatterer."));
                            }
                        return 0;
                    })))).then(literal("config").then(argument("name", StringArgumentType.string())
                        .suggests((ctx, abc) -> SharedSuggestionProvider.suggest(CURRENT_INSTANCE.scattererConfigNames(), abc)).executes((ctx) -> {
                        JsonElement ret = ScatterSpawnConfig.INSTANCE.getScattererConfig().get(ctx.getArgument("name", String.class));
                        if(ret == null) {
                            ctx.getSource().sendFailure(Component.literal("Couldn't find element"));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> Component.literal(GSON.toJson(ret)), false);
                        return 0;
                    }).then(argument("value", StringArgumentType.string()).executes((ctx) -> {
                            String name = ctx.getArgument("name", String.class);
                            String value = ctx.getArgument("value", String.class);

                            ScatterSpawnConfig.INSTANCE.getScattererConfig().add(name, new JsonPrimitive(value));

                            ctx.getSource().sendSuccess(() -> Component.literal("Done!"), false);
                            ScatterSpawnConfig.INSTANCE.dirty();

                        return 0;
                    }))))
                    .then(literal("player").then(argument("player", EntityArgument.player()).executes((ctx) -> {
                        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                        JsonObject playerConfig = ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs().get(player.getStringUUID()).getAsJsonObject();

                        if(ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs().has(player.getStringUUID())) {
                            ctx.getSource().sendSuccess(() -> Component.literal(GSON.toJson(playerConfig)), false);
                        } else
                            ctx.getSource().sendSuccess(() -> Component.literal("{}"), false);

                        return 0;
                    }).then(argument("name", StringArgumentType.string())
                        .suggests((ctx, abc) -> SharedSuggestionProvider.suggest(CURRENT_INSTANCE.playerConfigNames(), abc)).executes((ctx) -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            JsonObject playerConfig = ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs().get(player.getStringUUID()).getAsJsonObject();


                            if(playerConfig == null) {
                                ctx.getSource().sendFailure(Component.literal("Couldn't find element"));
                                return 0;
                            }
                            JsonElement ret = playerConfig.get(ctx.getArgument("name", String.class));

                            if(ret == null) {
                                ctx.getSource().sendFailure(Component.literal("Couldn't find element"));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(GSON.toJson(ret)), false);
                            return 0;
                        }).then(argument("value", StringArgumentType.string()).executes((ctx) -> {
                            try {

                                String name = ctx.getArgument("name", String.class);
                                String value = ctx.getArgument("value", String.class);

                                ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                JsonElement playerConfig1 = ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs().get(player.getStringUUID());
                                JsonObject playerConfig;
                                if (playerConfig1 == null) {
                                    playerConfig = new JsonObject();
                                    ScatterSpawnConfig.INSTANCE.getScattererPlayerConfigs().add(player.getStringUUID(), playerConfig);
                                } else
                                    playerConfig = playerConfig1.getAsJsonObject();

                                playerConfig.add(name, new JsonPrimitive(value));
                                ScatterSpawnConfig.INSTANCE.dirty();

                                ctx.getSource().sendSuccess(() -> Component.literal("Done!"), false);
                            } catch (Exception ex) { ex.printStackTrace(); }
                            return 0;
                        })))))
            );
        });
    }

}

package wtf.boomy.skinchanger.client.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.apache.commons.io.IOUtils;

import wtf.boomy.skinchanger.client.utils.AccessPlayerListEntry;
import wtf.boomy.skinchanger.client.utils.ThreadFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class SkinChangerClientCommand implements ClientCommandPlugin {
    
    private static final ThreadFactory factory = new ThreadFactory("SkinChanger");
    
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> commandDispatcher) {
        commandDispatcher.register(ArgumentBuilders.literal("skinchanger").executes(
                source -> {
                    source.getSource().sendFeedback(new LiteralText(Formatting.RED + "Please specify a player."));
                    return 1;
                }
        ).then(ArgumentBuilders.argument("name", StringArgumentType.word()).executes(ctx -> {
            handleIncomingName(StringArgumentType.getString(ctx, "name"), ctx);
            
            return 1;
        })));
    }
    
    private void handleIncomingName(String name, CommandContext<CottonClientCommandSource> ctx) throws CommandSyntaxException {
        if (name.contains(" ")) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "Please only supply 1 argument.")).create();
        }
    
        if (name.length() < 2) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "The provided username was too short.")).create();
        }
    
        if (name.length() > 16) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "The provided username was too long.")).create();
        }
    
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
    
        if (networkHandler == null) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "An illegal exception occurred. (NetworkHandler null)")).create();
        }
    
        PlayerListEntry entry = networkHandler.getPlayerListEntry(MinecraftClient.getInstance().getSession().getProfile().getId());
        
        if (entry == null) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "An illegal exception occurred. (PlayerList null)")).create();
        }
    
        scheduleProfileRetrieval(name, entry, ctx.getSource());
    }
    
    private void scheduleProfileRetrieval(String username, PlayerListEntry entry, CottonClientCommandSource ctx) {
        factory.runAsync(() -> {
            ctx.sendFeedback(new LiteralText(Formatting.GREEN + "Loading..."));
            
            GameProfile profile = getProfile(username);
    
            if (profile == null) {
                ctx.sendError(new LiteralText(Formatting.RED + "Unable to find a player with that username."));
                
                return;
            }
    
            Map<MinecraftProfileTexture.Type, Identifier> clientTextures = ((AccessPlayerListEntry) entry).getTextures();
    
            MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, identifier, minecraftProfileTexture) -> {
                clientTextures.put(type, identifier);
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    String model = minecraftProfileTexture.getMetadata("model");
            
                    if (model == null) {
                        model = "default";
                    }
            
                    ((AccessPlayerListEntry) entry).setModel(model);
                }
        
            }, true);
    
            HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(
                    Formatting.GRAY + "Username: " + Formatting.AQUA + profile.getName() + "\n" +
                           Formatting.GRAY +  "UUID: " + Formatting.AQUA + profile.getId().toString())
            );
            MutableText playerProfile = new LiteralText(profile.getName()).styled(style -> style.withColor(Formatting.AQUA).withHoverEvent(event));
    
            ctx.sendFeedback(new LiteralText(Formatting.GREEN + "Successfully set skin to ").append(playerProfile));
        });
    }
    
    private static GameProfile getProfile(String username) {
        String baseURL = "https://api.ashcon.app/mojang/v2/user/" + username;
        
        try {
            String data = IOUtils.toString(new URL(baseURL), StandardCharsets.UTF_8);
            
            JsonObject object = new JsonParser().parse(data).getAsJsonObject();
            
            if (object.has("code")) {
                int code = object.get("code").getAsInt();
                
                if (code >= 400 && code < 500) {
                    return null;
                }
            }
            
            if (object.has("username")) {
                username = object.get("username").getAsString();
            }
            
            return object.has("uuid") ? new GameProfile(UUID.fromString(object.get("uuid").getAsString()), username) : null;
        } catch (IOException | JsonParseException | IllegalStateException ex) {
            ex.printStackTrace();
            
            return null;
        }
    }
}

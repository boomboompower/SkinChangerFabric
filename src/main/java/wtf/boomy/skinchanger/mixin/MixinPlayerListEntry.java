package wtf.boomy.skinchanger.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import wtf.boomy.skinchanger.client.utils.AccessPlayerListEntry;

import java.util.Map;

/**
 * Allows us to get the texture map from the player class & set the model type of the player
 */
@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry implements AccessPlayerListEntry {
    
    @Final
    @Shadow
    private Map<MinecraftProfileTexture.Type, Identifier> textures;
    
    /**
     * A getter for the private "textures" field found in the PlayerListEntry class
     *
     * @return a map of ProfileTextures pointing to Identifier's
     */
    public Map<MinecraftProfileTexture.Type, Identifier> getTextures() {
        return this.textures;
    }
    
    /**
     * Generates a setter method for the model field
     *
     * @param model the new model type
     */
    @Accessor("model")
    public abstract void setModel(String model);
}

package wtf.boomy.skinchanger.client.utils;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface AccessPlayerListEntry {
    
    public Map<MinecraftProfileTexture.Type, Identifier> getTextures();
    
    public void setModel(String model);
}

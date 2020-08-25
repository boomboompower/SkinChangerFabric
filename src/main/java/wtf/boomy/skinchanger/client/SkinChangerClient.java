package wtf.boomy.skinchanger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkinChangerClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        registerCommands();
    }
    
    private void registerCommands() {
        /*
         * See:
         *  - CapeFeatureRenderer
         *  - AbstractClientPlayerEntity
         *  - PlayerListEntry
         */
        
    }
}

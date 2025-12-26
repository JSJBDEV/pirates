package ace.actually.pirates.mixin;

import ace.actually.pirates.util.BuiltinResourcePackSource;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.function.Consumer;

@Mixin(BuiltInPackSource.class)
public class BuiltinPackSourceMixin {


    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void addBuiltinResourcePacks(Consumer<Pack> consumer, CallbackInfo ci) {
        if ((Object) this instanceof ClientPackSource) {
            Pack.readMetaAndCreate(
                    "eureka_ships",
                    Component.literal("Eureka Ships"),
                    false,
                    a->new PathPackResources("eureka_ships", Path.of("pirates/datapacks/eureka_ships/"),true),
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    new BuiltinResourcePackSource());
        }
    }
}

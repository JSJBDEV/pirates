package ace.actually.pirates.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;

public class BuiltinResourcePackSource implements PackSource {
    @Override
    public Component decorate(Component component) {
        return Component.literal("Built-In pack (Pirates)");
    }

    @Override
    public boolean shouldAddAutomatically() {
        return true;
    }
}

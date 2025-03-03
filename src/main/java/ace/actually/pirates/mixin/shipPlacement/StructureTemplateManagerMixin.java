package ace.actually.pirates.mixin.shipPlacement;

import ace.actually.pirates.structures.CanRemoveTemplate;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(value = StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin implements CanRemoveTemplate {

    @Final
    @Shadow
    private Map<Identifier, Optional<StructureTemplate>> templates;

    @Shadow
    protected abstract Optional<StructureTemplate> loadTemplate(Identifier identifier);

    @Shadow public abstract void unloadTemplate(Identifier id);

    @Inject(method = "getTemplate", at = @At("HEAD"), cancellable = true)
    public void getTemplateMixin(Identifier id, CallbackInfoReturnable<Optional<StructureTemplate>> cir) {

        Optional<StructureTemplate> template = this.templates.computeIfAbsent(id, this::loadTemplate);

        if (template.isPresent() && !template.get().getAuthor().equals("dirty") && id.getNamespace().equals("pirates") && id.getPath().startsWith("ship/")) {
            template.get().setAuthor("pirate-ship");
        }

        cir.setReturnValue(template);
    }

    @Override
    public boolean pirates$unload(StructureTemplate template) {
        Optional<Identifier> key = templates.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent() && entry.getValue().get().equals(template))
                .map(Map.Entry::getKey)
                .findFirst();

        key.ifPresent(this::unloadTemplate);
        return key.isPresent();
    }
}

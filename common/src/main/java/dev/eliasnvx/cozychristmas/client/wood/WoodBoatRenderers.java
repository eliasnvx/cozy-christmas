package dev.eliasnvx.cozychristmas.client.wood;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;

/**
 * Boats and chest boats of our woods, drawn by vanilla's boat renderer with vanilla's models. The renderer looks the
 * texture up from the model layer's name: {@code textures/entity/boat/<wood>.png} and {@code chest_boat/<wood>.png}.
 */
public final class WoodBoatRenderers {

    private WoodBoatRenderers() {
    }

    public static void register() {
        for (WoodSet set : CozyWoods.ALL) {
            ModelLayerLocation boat = new ModelLayerLocation(CozyChristmas.id("boat/" + set.name), "main");
            ModelLayerLocation chestBoat = new ModelLayerLocation(CozyChristmas.id("chest_boat/" + set.name), "main");
            EntityModelLayerRegistry.register(boat, BoatModel::createBoatModel);
            EntityModelLayerRegistry.register(chestBoat, BoatModel::createChestBoatModel);
            EntityRendererRegistry.register(set.boat, context -> new BoatRenderer(context, boat));
            EntityRendererRegistry.register(set.chestBoat, context -> new BoatRenderer(context, chestBoat));
        }
    }
}

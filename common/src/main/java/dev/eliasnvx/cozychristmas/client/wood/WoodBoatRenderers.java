package dev.eliasnvx.cozychristmas.client.wood;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.eliasnvx.cozychristmas.CozyChristmas;
import dev.eliasnvx.cozychristmas.wood.CozyWoods;
import dev.eliasnvx.cozychristmas.wood.WoodSet;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** Registers boat and chest boat layers and renderers for each wood. */
public final class WoodBoatRenderers {

    private WoodBoatRenderers() {
    }

    public static void register() {
        for (WoodSet set : CozyWoods.ALL) {
            ModelLayerLocation boat = new ModelLayerLocation(CozyChristmas.id("boat/" + set.name), "main");
            ModelLayerLocation chestBoat = new ModelLayerLocation(CozyChristmas.id("chest_boat/" + set.name), "main");
            EntityModelLayerRegistry.register(boat, BoatModel::createBodyModel);
            EntityModelLayerRegistry.register(chestBoat, ChestBoatModel::createBodyModel);
            var boatTex = CozyChristmas.id("textures/entity/boat/" + set.name + ".png");
            var chestTex = CozyChristmas.id("textures/entity/chest_boat/" + set.name + ".png");
            EntityRendererRegistry.register(set.boat, ctx -> new CozyBoatRenderer(ctx, boat, boatTex, false));
            EntityRendererRegistry.register(set.chestBoat, ctx -> new CozyBoatRenderer(ctx, chestBoat, chestTex, true));
        }
    }
}

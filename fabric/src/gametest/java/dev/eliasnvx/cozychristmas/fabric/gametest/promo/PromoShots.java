package dev.eliasnvx.cozychristmas.fabric.gametest.promo;

import dev.eliasnvx.cozychristmas.fabric.gametest.GoblinsLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.KrampusLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.PresentsLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.SleighLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.SnowmenLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.StockingsLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.TreeLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.WoodLookClientTest;
import dev.eliasnvx.cozychristmas.fabric.gametest.WorkshopLookClientTest;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Promo screenshots: turns on a shader pack through Iris (when tools/promo/shoot.sh put Iris on the classpath), then
 * runs the look tests whose scenes make the store-page shots. Run it alone with {@code tools/promo/shoot.sh};
 * {@code PROMO_ONLY=workshop,krampus} picks scenes.
 */
public class PromoShots implements FabricClientGameTest {

    private static final String PACKS = "tools/promo/shaderpacks";
    private static final String PACK = "ComplementaryReimagined_r5.9.3.zip";

    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(mc -> enableShaders());
        Map<String, FabricClientGameTest> scenes = new LinkedHashMap<>();
        scenes.put("hero", new PromoHero());
        scenes.put("workshop", new WorkshopLookClientTest());
        scenes.put("krampus", new KrampusLookClientTest());
        scenes.put("goblins", new GoblinsLookClientTest());
        scenes.put("snowmen", new SnowmenLookClientTest());
        scenes.put("stockings", new StockingsLookClientTest());
        scenes.put("tree", new TreeLookClientTest());
        scenes.put("sleigh", new SleighLookClientTest());
        scenes.put("presents", new PresentsLookClientTest());
        scenes.put("wood", new WoodLookClientTest());
        String only = System.getenv("PROMO_ONLY");
        for (var scene : scenes.entrySet()) {
            if (only == null || java.util.List.of(only.split(",")).contains(scene.getKey())) {
                scene.getValue().runTest(context);
            }
        }
    }

    private static void enableShaders() {
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            Path packs = (Path) iris.getMethod("getShaderpacksDirectory").invoke(null);
            Path source = Path.of(System.getProperty("user.dir")).resolve("../../../..").normalize()
                    .resolve(PACKS).resolve(PACK);
            Files.createDirectories(packs);
            Files.copy(source, packs.resolve(PACK), StandardCopyOption.REPLACE_EXISTING);
            Object config = iris.getMethod("getIrisConfig").invoke(null);
            config.getClass().getMethod("setShaderPackName", String.class).invoke(config, PACK);
            config.getClass().getMethod("setShadersEnabled", boolean.class).invoke(config, true);
            config.getClass().getMethod("save").invoke(config);
            iris.getMethod("reload").invoke(null);
        } catch (ClassNotFoundException noIris) {
            // vanilla shots
        } catch (ReflectiveOperationException | IOException e) {
            throw new AssertionError("couldn't turn on the shader pack", e);
        }
    }
}

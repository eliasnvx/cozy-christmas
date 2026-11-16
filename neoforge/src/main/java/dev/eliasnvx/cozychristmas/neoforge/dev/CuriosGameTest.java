package dev.eliasnvx.cozychristmas.neoforge.dev;

import com.mojang.authlib.GameProfile;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticSlot;
import dev.eliasnvx.cozychristmas.cosmetic.CosmeticsManager;
import dev.eliasnvx.cozychristmas.registry.CozyItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;

import java.util.UUID;

/**
 * Runs only with Curios installed ({@code ./gradlew :neoforge:runGameTestServer -Pcompat=curios}): our data gives
 * players the slots, our cosmetics fit them, and one worn there counts. Ported from Femboy Mod.
 */
public final class CuriosGameTest {

    private CuriosGameTest() {
    }

    public static void cosmeticsInCuriosSlotsCount(GameTestHelper helper) {
        // not logged in: Curios syncs its slots to joining players, which a test connection can't carry
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "curios-test"), ClientInformation.createDefault());
        ItemStack scarf = new ItemStack(CozyItems.SCARF.get());
        helper.assertTrue(CuriosSlotTypes.getItemSlotTypes(scarf, player).containsKey("necklace"), "a scarf fits the necklace slot");
        helper.assertTrue(CuriosSlotTypes.getItemSlotTypes(new ItemStack(CozyItems.POMPOM_HAT.get()), player).containsKey("head"),
                "a hat fits the head slot");
        helper.assertTrue(CuriosSlotTypes.getItemSlotTypes(new ItemStack(CozyItems.sweaters().getFirst().get()), player)
                .containsKey("body"), "a sweater fits the body slot");
        helper.assertTrue(CuriosSlotTypes.getItemSlotTypes(new ItemStack(CozyItems.MITTENS.get()), player).containsKey("hands"),
                "mittens fit the hands slot");
        var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
        // Curios 17 fills a player's slots when they join a level, which this offline player never does
        inventory.loadDatapacks();
        helper.assertTrue(inventory.getStacksHandler("necklace").isPresent(), "players have the necklace slot (data/cozychristmas/curios)");
        inventory.setEquippedCurio("necklace", 0, scarf);
        helper.assertTrue(CosmeticsManager.worn(player).get(CosmeticSlot.NECK).is(CozyItems.SCARF.get()), "a scarf in Curios is worn");
        inventory.setEquippedCurio("necklace", 0, ItemStack.EMPTY);
        helper.succeed();
    }
}

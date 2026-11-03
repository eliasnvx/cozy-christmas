# Cozy Christmas: Santa's Workshop, Krampus & Presents

A Christmas adventure mod for Minecraft: the world hides Santa's workshop, Krampus's lair and camps of present goblins, and every present holds a gift, a lump of coal or a bang, depending on your karma.

> Early development. Nothing to play yet.

## Versions
| Minecraft | Loaders |
| --- | --- |
| 26.3 | NeoForge, Fabric |

Requires [Architectury API](https://modrinth.com/mod/architectury-api) and [GeckoLib](https://modrinth.com/mod/geckolib) (Fabric also needs Fabric API).

## Knitwear
Four cosmetic slots next to your inventory (hat, scarf, sweater, mittens), worn on top of armor: right-click a piece or shift-click it into its slot. Sweaters come with snowflakes, a reindeer, a cat, a bee or a present; the pompom hat, scarf and mittens take any dye, like leather armor. Mittens and sleeves show in first person too.

Any item can be made wearable with the `cozychristmas:cosmetic` component (`{"slot": "head"}`); it is then worn, though only the mod's own pieces have a look.

**Toasty set:** a sweater, hat, scarf and mittens together keep you from freezing in powder snow and make you 10% faster on snow. The tooltip counts the pieces you wear. Pieces are the item tag `#cozychristmas:toasty_set`, the snow is the block tag `#cozychristmas:toasty_snow`.

## Presents
Presents hide something that depends on how you've behaved: a real gift, a lump of coal or a "boom" of confetti and snow (a prank: 4-6 damage and a push, blocks stay intact). A present that's going to go boom ticks and shakes when you stand close, so the careful notice. Break a present to carry it and give it to someone else.

The **wrapping table** wraps any stack into a present in one of four designs and signs it with your name. Wrap TNT to make your own prank. When someone else opens a gift you wrapped, Santa notices.

What's in a present is the loot tables `cozychristmas:presents/gift` and `cozychristmas:presents/coal`; the tags `#cozychristmas:wraps_as_boom`, `#cozychristmas:wrapping_paper` and `#cozychristmas:unwrappable` tune the table.

## Karma
Every player has a hidden karma from -100 to +100. Hurting villagers and other peaceful folk lowers it; it drifts back toward zero by about one point per in-game day. Nice players mostly find gifts in their presents, naughty ones find coal.

Mobs that count are in the entity tag `#cozychristmas:karma_protected`, so data packs can change them.

## Config
`config/cozychristmas-common.json`: season dates (`real_date`, `always` or `never`), karma thresholds and amounts, present odds per karma tier, the warning radius and the boom (damage, radius, knockback, `breaks_blocks`), the Toasty set's bonuses (`keeps_warm`, `snow_speed_bonus`). Missing options are filled with defaults; a broken file is kept as `.broken` and replaced.

Operators: `/cozychristmas karma get|set|add <players> [value]`, `/cozychristmas season`.

## Building
Java 25.
```bash
./gradlew build
```

## License
MIT. Part of the [Cubimoon](https://cubimoon.com) mods.

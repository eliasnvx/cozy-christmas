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
Presents hide something that depends on how you've behaved: a real gift, a lump of coal or a "boom" of confetti and snow (a prank: 4-6 damage and a push, blocks stay intact). A present that's going to go boom ticks and shakes when you stand close, so the careful notice. Break a present to carry it and give it to someone else. During the Christmas season presents now and then turn up in the snow around you.

The **wrapping table** wraps any stack into a present in one of four designs and signs it with your name. Wrap TNT to make your own prank. When someone else opens a gift you wrapped, Santa notices.

What's in a present is the loot tables `cozychristmas:presents/gift` and `cozychristmas:presents/coal`; the tags `#cozychristmas:wraps_as_boom`, `#cozychristmas:wrapping_paper` and `#cozychristmas:unwrappable` tune the table.

## Santa's workshop
Very rarely, in snowy plains and taiga, you find Santa's workshop: a snowy square with a decorated tree and the sleigh at the way in, the hall where elves hammer at their workbenches, Santa's house, the present warehouse and a stable of reindeer.

- **Santa** trades for emeralds (presents, saddles, knitwear, treats) and buys back your coal. He knows his list: nice players pay less and, during the Christmas season, get a present once a day; naughty ones he turns away. Hurt him or an elf, or take a present from the warehouse, and he comes after you.
- **Elves** work at the workbenches and carry finished presents around. They're harmless: they run from anyone with a weapon out and follow a cookie.
- **Reindeer** are mounts. Feed a wild one carrots to tame it, golden carrots to breed. Saddled, it flies during the Christmas season: jump to take off, look where you want to go, hold jump to climb. Wild herds live in the taiga too. Feeding reindeer is a good deed.

Where the workshop generates is the biome tag `#cozychristmas:has_structure/santas_workshop`, how often the structure set `cozychristmas:santas_workshop`; Santa's goods are the trade sets `cozychristmas:santa/goods` and `cozychristmas:santa/buying`; elves work at `#cozychristmas:elf_workstations`.

## Karma
Every player has a hidden karma from -100 to +100. Hurting villagers, elves and other peaceful folk or stealing from Santa lowers it; giving gifts and feeding reindeer raises it; it drifts back toward zero by about one point per in-game day. Nice players mostly find gifts in their presents, naughty ones find coal.

Mobs that count are in the entity tag `#cozychristmas:karma_protected`, so data packs can change them.

## Config
`config/cozychristmas-common.json`: season dates (`real_date`, `always` or `never`), karma thresholds and amounts, present odds per karma tier, the warning radius, the boom (damage, radius, knockback, `breaks_blocks`), presents in the snow (`presents.snow`), the Toasty set's bonuses (`keeps_warm`, `snow_speed_bonus`), Santa and the elves (`workshop`: health, anger, the nice discount, daily gifts), reindeer (`reindeer`: when they fly, `season` / `always` / `never`, speeds, taming chance, wild herds). Missing options are filled with defaults; a broken file is kept as `.broken` and replaced.

Operators: `/cozychristmas karma get|set|add <players> [value]`, `/cozychristmas season`.

## Building
Java 25.
```bash
./gradlew build
```

## License
MIT. Part of the [Cubimoon](https://cubimoon.com) mods.

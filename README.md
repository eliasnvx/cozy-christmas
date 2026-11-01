# Cozy Christmas: Santa's Workshop, Krampus & Presents

A Christmas adventure mod for Minecraft: the world hides Santa's workshop, Krampus's lair and camps of present goblins, and every present holds a gift, a lump of coal or a bang, depending on your karma.

> Early development. Nothing to play yet.

## Versions
| Minecraft | Loaders |
| --- | --- |
| 26.3 | NeoForge, Fabric |

Requires [Architectury API](https://modrinth.com/mod/architectury-api) and [GeckoLib](https://modrinth.com/mod/geckolib) (Fabric also needs Fabric API).

## Karma
Every player has a hidden karma from -100 to +100. Hurting villagers and other peaceful folk lowers it; it drifts back toward zero by about one point per in-game day. Nice players mostly find gifts in their presents, naughty ones find coal.

Mobs that count are in the entity tag `#cozychristmas:karma_protected`, so data packs can change them.

## Config
`config/cozychristmas-common.json`: season dates (`real_date`, `always` or `never`), karma thresholds and amounts, present odds per karma tier. Missing options are filled with defaults; a broken file is kept as `.broken` and replaced.

Operators: `/cozychristmas karma get|set|add <players> [value]`, `/cozychristmas season`.

## Building
Java 25.
```bash
./gradlew build
```

## License
MIT. Part of the [Cubimoon](https://cubimoon.com) mods.

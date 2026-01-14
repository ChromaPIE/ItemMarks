# Item Marks

A Minecraft 1.7.10 mod for displaying custom marks on item icons.

## Features

- 1-4 character marks on items
- Match by Item ID, Ore Dictionary (with wildcards), or NBT
- Adaptive sizing for longer marks
- GUI manager with search and NBT picker

## Installation

1. Install Minecraft Forge 1.7.10
2. Install [ModularUI2](https://github.com/GTNewHorizons/ModularUI2)
3. Place mod JAR in `mods` folder

## Quick Start

Press **M** to open the manager. Click **Add** or **From Hand** to create entries.

## Matching Rules

### Item ID

| Format          | Matches          |
|-----------------|------------------|
| `modid:item`    | Any metadata     |
| `modid:item:16` | Metadata 16 only |

### Ore Dictionary

| Pattern            | Matches                    |
|--------------------|----------------------------|
| `ingotSteel`       | Exact name                 |
| `ingot*`           | Starts with                |
| `*Steel`           | Ends with                  |
| `*gold*`           | Contains                   |
| `ingot*\|dust*`    | OR: ingot* or dust*        |

Longer patterns take priority; wildcards reduce priority.

### NBT Path

| Syntax    | Example        |
|-----------|----------------|
| `key`     | `Damage`       |
| `key.sub` | `display.Name` |
| `list[0]` | First element  |
| `list[*]` | Any element    |
| `*`       | Any key        |
| `*.sub`   | Any key's sub  |

### NBT Value

| Value     | Meaning         |
|-----------|-----------------|
| `123`     | Exact match     |
| `*`       | Field exists    |
| `!`       | Field missing   |
| `a=1&b=2` | Multi-condition |

## Priority

Score-based matching (higher wins):

| Condition | Base Score | Modifiers                            |
|-----------|------------|--------------------------------------|
| Item ID   | 10000      | +500 NBT, +10 Meta                   |
| Ore Dict  | 1000       | +length, -100 per wildcard, +500 NBT |
| NBT only  | 500        |                                      |

For OR patterns (`a|b`), the lowest-scoring pattern determines the score.

## Configuration

| Option        | Default  |
|---------------|----------|
| Display Marks | ON       |
| Position      | Top Left |
| Scale         | 100%     |
| Adaptive Size | ON       |

Positions: Top Left, Top Right, Bottom Left, Middle

## Files

| File                    | Content    |
|-------------------------|------------|
| `itemmarks.txt`         | Entry data |
| `itemmarks_config.json` | Settings   |

Entry format: `Mark|ItemID:Meta|OreDict|NBTPath|NBTValue`

## Examples

| Mark | Item ID             | Ore Dict        | NBT Path | NBT Value | Description      |
|------|---------------------|-----------------|----------|-----------|------------------|
| `D`  | `minecraft:diamond` |                 |          |           | All diamonds     |
| `E`  |                     |                 | `ench`   | `*`       | Enchanted items  |
| `Cu` |                     | `*Copper`       |          |           | Any copper items |
| `M`  |                     | `ingot*\|dust*` |          |           | Ingots or dusts  |
| `!`  |                     |                 | `Charge` | `!`       | Uncharged items  |

## Dependencies

- Minecraft 1.7.10 + Forge
- ModularUI2

## License

MIT

## Credits

- [ModularUI2](https://github.com/GTNewHorizons/ModularUI2) - GUI framework
- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) - Code injection framework
- Claude. Most importantly. - Passed the VIBE check. Love you man.

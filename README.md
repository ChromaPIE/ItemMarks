# Item Marks

A Minecraft 1.7.10 mod that allows players to display custom marks on item icons for quick identification of specific items.

## Features

- Render 1-2 character custom marks on item icons
- Match items by Item ID, metadata (Meta), and NBT data
- Support NBT-only matching (no Item ID required) to match different items with the same NBT structure
- Visual GUI management interface
- Configurable mark position and size

## Installation

1. Ensure Minecraft Forge 1.7.10 is installed
2. Ensure [ModularUI2](https://github.com/GTNewHorizons/ModularUI2) dependency is installed
3. Place the mod JAR file into the `mods` folder

## Usage

### Opening the Manager

Press **M** key (configurable in Controls settings) to open the Item Marks Manager.

### Manager Interface Layout

```
┌──────────────────────────────────────────┐
│ [C]      Item Marks Manager      [?] │
├──────────────────────────────────────────┤
│                                      │
│  Entry List (newest at top)          │
│  [Mark] ItemID:Meta {NBT Condition}  │
│                                      │
├──────────────────────────────────────────┤
│ [Add] [From Hand]    [🔍 Search...] │
└──────────────────────────────────────────┘
```

- **C Button**: Open configuration panel
- **? Button**: Open help panel
- **Add**: Create a new mark entry (appears at top of list)
- **From Hand**: Get Item ID from held item and create new entry
- **Search**: Real-time filter entries by Item ID

### Configuration Panel

- **R Button** (top right): Reset all entries (requires confirmation)
- **Enabled**: Toggle mark rendering on/off
- **Position**: Cycle through mark positions (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
- **Scale**: Adjust mark size (50% - 300%)

### Entry List Operations

- **Left-click**: Edit entry
- **Right-click**: Open context menu
  - **Dupe**: Create new entry with same content (for creating variations)
  - **Delete**: Delete entry

## Entry Editor

### Field Descriptions

| Field         | Description                          | Examples                  |
|---------------|--------------------------------------|---------------------------|
| **Mark**      | 1-2 characters displayed on the item | `A`, `★`, `01`            |
| **Item ID**   | Minecraft item registry name         | `minecraft:diamond_sword` |
| **NBT Path**  | Path to access NBT data              | `tag.display.Name`        |
| **NBT Value** | Expected value to match              | `123`, `*`, `!`           |

### Item ID Format

| Format          | Description                             |
|-----------------|-----------------------------------------|
| `modid:item`    | Match item with metadata 0              |
| `modid:item:16` | Match item with metadata 16             |
| `modid:item:*`  | Match item with any metadata            |
| *(empty)*       | No item restriction (NBT-only matching) |

### NBT Path Syntax

| Syntax      | Description                            | Example          |
|-------------|----------------------------------------|------------------|
| `key`       | Direct access to root-level key        | `Damage`         |
| `key.sub`   | Access nested key                      | `display.Name`   |
| `list[0]`   | Access first element of list           | `ench[0]`        |
| `list[*]`   | Match any element in list              | `ench[*]`        |
| `*`         | Match any key at current level         | `*`              |
| `*.sub`     | Match any key's nested field           | `*.FluidName`    |
| `foo.*.bar` | Wildcard in middle of path             | `Tanks.*.Amount` |
| *(empty)*   | Multi-condition matching at root level | -                |

### NBT Value Matching

| Value     | Description                                                    |
|-----------|----------------------------------------------------------------|
| `123`     | Exact match (auto-strips type suffixes s/b/l/f/d)              |
| `*`       | Match if field exists                                          |
| `!`       | Match if field does not exist                                  |
| `a=1&b=2` | Multi-condition matching (at root level or when path is empty) |

### NBT Picker

Click **NBT...** button to open the NBT Picker (requires holding an item with NBT data):

- Visual display of item's NBT structure
- **Left-click** `[+]/[-]` to expand/collapse compound tags
- **Right-click** on a field to select an action:
  - **= Value**: Match the field's current value
  - **Exists (\*)**: Match if field exists
  - **Not (!)**: Match if field does not exist

The NBT Picker is draggable and does not block interaction with the editor window below.

## Configuration

Click the **C** button in the manager to open the configuration panel:

| Option            | Description                     | Default  |
|-------------------|---------------------------------|----------|
| **Display Marks** | Enable/disable mark rendering   | ON       |
| **Mark Position** | Position of mark on item icon   | Top Left |
| **Mark Scale**    | Scale ratio of marks (50%-300%) | 100%     |

### Mark Position Options

- **Top Left**: Mark displays at top-left corner of item icon
- **Top Right**: Mark displays at top-right corner of item icon
- **Bottom Left**: Mark displays at bottom-left corner of item icon
- **Middle**: Mark displays at center of item icon

## Match Priority

When multiple entries could match the same item, the following priority is used:

1. **Item + NBT + Meta** (most specific)
2. **Item + NBT** (any metadata)
3. **Item + Meta** (no NBT condition)
4. **Item only** (no NBT, no meta restriction)
5. **NBT + Meta** (no item restriction)
6. **NBT only** (no item, no meta restriction)

## Configuration Files

Mod configuration is stored in the Minecraft config directory:

| File                    | Content                                       |
|-------------------------|-----------------------------------------------|
| `itemmarks.txt`         | Mark entry data                               |
| `itemmarks_config.json` | Mod settings (position, scale, enabled state) |

### Entry File Format

One entry per line, format:
```
Mark|ItemID:Meta|NBTPath|NBTValue
```

Examples:
```
★|minecraft:diamond_sword:*||
A|minecraft:potion:0|Potion|minecraft:strength
!|gtceu:electric_item:*|Charge|!
```

## Usage Examples

### Example 1: Mark All Diamond Swords

- Mark: `◇`
- Item ID: `minecraft:diamond_sword:*`
- NBT Path: *(empty)*
- NBT Value: *(empty)*

### Example 2: Mark Enchanted Items

- Mark: `E`
- Item ID: *(empty)*
- NBT Path: `ench`
- NBT Value: `*`

### Example 3: Mark Specific Potion

- Mark: `S`
- Item ID: `minecraft:potion:0`
- NBT Path: `Potion`
- NBT Value: `minecraft:strength`

### Example 4: Mark Uncharged Electric Items

- Mark: `!`
- Item ID: *(empty)*
- NBT Path: `Charge`
- NBT Value: `!`

### Example 5: Multi-Condition Matching

- Mark: `OK`
- Item ID: `modid:machine:0`
- NBT Path: *(empty)*
- NBT Value: `active=1&energy=*`

### Example 6: Wildcard Path Matching (e.g., Fluid Tanks)

- Mark: `O₂`
- Item ID: *(empty)*
- NBT Path: `*.FluidName`
- NBT Value: `oxygen`

This matches any item where any root-level NBT key contains a `FluidName` field with value `oxygen`.

## Key Bindings

| Action          | Function               | Default |
|-----------------|------------------------|---------|
| Open Item Marks | Open manager interface | M       |

Configurable in Game Settings → Controls → Item Marks category.

## Dependencies

- Minecraft 1.7.10
- Forge
- ModularUI2

## License

MIT

## Credits

- [ModularUI2](https://github.com/GTNewHorizons/ModularUI2) - GUI framework
- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) - Code injection framework
- Claude. Most importantly. - Passed the VIBE check. Love you man.

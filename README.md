# PluginGui

**Version:** Minecraft 26.1+ (Paper) · **Type:** Plugin

A GUI that lists every plugin installed on your server and the commands each one registers, so owners,
admins and players can reach commands without memorising them.

## Features

**From the original guide**
- Built-in menu listing all installed plugins (`/plugingui`)
- Custom menu support: Nexo and ItemsAdder items as icons, `custom-model-data`, `item-model`, glyphs in titles
- Categories that organise commands (Building, Storage, PvP, Economy, Guides, Info by default)
- Files: `config.yml`, `messages.yml`, `categories.yml`, `menus/*.yml`

**Added extras**
- Pagination, plugin search (compass), and a back button on every menu
- In-game category editor: rename, description, icon, choose commands, move, delete (matches the "Edit Gui" / "Edits Menu" screens)
- Left-click runs a command, right-click puts it in the chat box (commands that need arguments always go to chat)
- Player menus only show commands the player has permission for
- Each command shows description, usage, aliases, permission and whether *you* can use it
- A "Minecraft" entry for built-in server commands
- `/pgui plugin <name>`, `/pgui search <text>`, `/pcoms <category>`, tab completion
- Every text, item, layout and sound is editable in the config files

## Commands

| Command | Permission | What it does |
|---|---|---|
| `/plugingui` (`/pgui`) | `plugingui.admin` (op) | Opens the main menu with all plugins and commands |
| `/pgui reload` | `plugingui.admin` | Reloads all files you edited |
| `/pgui edit` | `plugingui.admin` | Opens the category editor |
| `/pgui plugin <name>` | `plugingui.admin` | Opens one plugin's commands |
| `/pgui search <text>` | `plugingui.admin` | Opens the plugin list filtered by name |
| `/playercommands` (`/pcoms`) | `plugingui.player` (everyone) | Opens the player category menu |

## Menus

| Menu | Layout |
|---|---|
| **Plugins Gui** | one enchanted book per plugin; bottom row: back, Edit Gui (47), close (49), prev/next, search (51) |
| **Plugin Name** | one command block per command; close at slot 49 |
| **'Player' Commands** | category icons on their slots; Help (47) and close (49) |
| **Edit Gui** | the same grid for admins; click a category to edit it, an empty slot to create one |
| **Edits Menu** | rename (name tag), description (sign), icon (anvil), commands (command block), move (chest), delete (TNT) |

## Files (created in `plugins/PluginGui/`)

- `config.yml` behaviour, hooks, shared buttons, click sound
- `messages.yml` every chat message (MiniMessage)
- `categories.yml` the player categories (also edited by the in-game editor)
- `menus/main.yml`, `commands.yml`, `player.yml`, `editor.yml` titles, borders, icons and lore

Icons accept `DIAMOND_SWORD`, `nexo:item_id`, `itemsadder:namespace:item_id`, and optionally
`custom-model-data` / `item-model`. For a resource-pack menu background, put the pack's glyph character
in a menu's `title`.

## Building

Requires JDK 25 (Paper 26.1+ runs on Java 25) and Gradle.

```
gradle build
```

The jar is `build/libs/PluginGui-1.0.0.jar`. Drop it into the server's `plugins/` folder.
If your Paper API version differs, change `paperApi` in `gradle.properties`.

## Building with GitHub (no installs needed)

Push this project to a GitHub repository. The workflow in `.github/workflows/build.yml` builds the jar
on every push. Open the repository's **Actions** tab, click the latest run, and download **PluginGui-jar**
from the Artifacts section. If the build fails, open the run's log and send me the error.

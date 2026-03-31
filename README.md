# ⏱ TrialTimer

> Configurable Trial Spawner cooldown — because 30 minutes is way too long.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green?style=flat-square)
![Paper](https://img.shields.io/badge/Paper%20%2F%20Purpur-supported-blue?style=flat-square)
![Version](https://img.shields.io/badge/Version-1.1.0-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

---

## 🔧 What does it do?

Vanilla Trial Spawners have a **hardcoded 30-minute cooldown** after a wave is completed.
TrialTimer lets you set that to whatever you want — down to a few seconds if needed.

No client-side mods required. Works fully server-side.

When a spawner finishes its cooldown, nearby players receive a **broadcast message** so they know it's ready to fight again.

---

## ⚙️ How does it work?

The 30-minute rule is hardcoded in Minecraft's source code, so datapacks can't touch it.
TrialTimer works around this by hooking into the spawner's internal state via **NMS reflection**:
when a spawner enters cooldown, the plugin overwrites `cooldownEndsAt` to your configured duration.
Minecraft then reads that value and considers the cooldown over — simple and clean.

---

## 📦 Installation

1. Drop `TrialTimer-x.x.x.jar` into your `plugins/` folder
2. Restart the server
3. Edit `plugins/TrialTimer/config.yml`
4. Run `/trialtimer reload`

---

## 🗂 config.yml

```yaml
# Cooldown duration in minutes. Supports decimals (e.g. 0.5 = 30 seconds).
# Vanilla default: 30 minutes
cooldown-minutes: 5.0

# Language for plugin messages.
# Available: en, de
language: en

# Broadcast a message to nearby players when a Trial Spawner finishes its cooldown.
broadcast:
  enabled: true
  radius: 64  # in blocks

# Debug mode — logs every cooldown modification to the console.
debug: false
```

### Quick reference — Minutes → Ticks

| Minutes | Seconds | Ticks  |
|---------|---------|--------|
| 0.5     | 30s     | 600    |
| 1       | 60s     | 1,200  |
| 5       | 300s    | 6,000  |
| 10      | 600s    | 12,000 |
| 30      | 1800s   | 36,000 *(vanilla)* |

---

## 💬 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trialtimer help` | Shows command overview | `trialtimer.use` |
| `/trialtimer info` | Shows current cooldown | `trialtimer.use` |
| `/trialtimer reload` | Reloads config + language | `trialtimer.reload` |

**Alias:** `/tt`

---

## 📢 Broadcast

When a Trial Spawner finishes its cooldown, all players within the configured radius receive a message:

```
[TrialTimer] A Trial Spawner at 42, 63, -128 is ready to fight again!
```

The message is fully customizable in the language files (`lang/messages_en.yml` / `lang/messages_de.yml`).

---

## 📊 PlaceholderAPI

TrialTimer supports **PlaceholderAPI** (optional — works without it too).

| Placeholder | Description |
|-------------|-------------|
| `%trialtimer_cooldown_minutes%` | Configured cooldown in minutes |
| `%trialtimer_cooldown_seconds%` | Configured cooldown in seconds |
| `%trialtimer_cooldown_ticks%` | Configured cooldown in ticks |

Use these in scoreboards, holograms, or any plugin that supports PlaceholderAPI.

---

## 🌍 Language Support

TrialTimer ships with **English** and **German** out of the box.
Switch via `language: de` in `config.yml`.

Language files are saved to `plugins/TrialTimer/lang/` and can be edited freely.

---

## 🔌 Compatibility

| Software | Supported |
|----------|-----------|
| Paper 1.21.x | ✅ |
| Purpur 1.21.x | ✅ |
| PlaceholderAPI | ✅ *(optional)* |
| Spigot | ❌ |
| Folia | ❌ |

> **Note:** The NMS reflection targets Mojang-mapped Paper/Purpur builds (1.21+).
> Enable `debug: true` if you run into issues — it logs the exact field names found at runtime.

---

## 📄 License

MIT — free to use, fork, and redistribute.
# InviteRewards 🏆

![Version](https://img.shields.io/badge/version-1.0--RELEASE-blue)
![Platform](https://img.shields.io/badge/platform-Spigot%201.16.1-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![License](https://img.shields.io/badge/license-MIT-yellow)

**InviteRewards** is a high-performance referral system for Minecraft servers, built with a modular architecture (**Core/Premium**) that enables full scalability and advanced management of invites, rewards, and statistics.

---

## ✨ Key Features

- Modular Core/Premium architecture
- Multi invite types (Global, VIP, Events)
- Fully dynamic action system
- Async database & leaderboard
- Anti-fraud (multi-account/IP validation)
- YAML-based translations

---

## ⚡ Executable Actions

InviteRewards includes a powerful **action engine** that allows full customization without coding.

### Supported Actions

| Action | Description |
| :--- | :--- |
| `[message]` | Send chat message |
| `[console]` | Execute console command |
| `[player_command]` | Execute command as player |
| `[sound]` | Play sound (`sound\|volume\|pitch`) |
| `[action_bar]` | Show action bar |
| `[title]` | Show title + subtitle |
| `[open_menu]` | Open GUI menu |
| `[close_menu]` | Close GUI |

### Example

```yaml
- "[message] &aInvite claimed!"
- "[console] give %player% diamond 1"
- "[sound] ENTITY_PLAYER_LEVELUP|1.0|1.2"
- "[title] &6Reward! subtitle:&eEnjoy your bonus!"
```

---

## 🔗 Placeholder System

InviteRewards provides a powerful placeholder system for dynamic messages, commands, and rewards.


### 👤 Player Placeholders

- `%inviterewards_player_name%`
- `%inviterewards_player_points%`
- `%inviterewards_player_code%`
- `%inviterewards_player_total_invites%`
- `%inviterewards_player_has_claimed%`
- `%inviterewards_player_invited_by%`


### 🎯 Target Placeholders

- `%inviterewards_target_name%`
- `%inviterewards_target_points%`
- `%inviterewards_target_code%`
- `%inviterewards_target_total_invites%`


### 🏷️ Invite Type Placeholders

- `%inviterewards_invite_type_id%`
- `%inviterewards_invite_type_name%`

---

## 🏆 Leaderboard Placeholders

Advanced placeholders for leaderboards and ranking systems.

### Player Ranking

- `%inviterewards_player_leaderboard_rank%` → Player rank (returns `100+` if out of range)
- `%inviterewards_target_leaderboard_rank%` → Target rank (context-based)


### Top 10 Leaderboard

| Placeholder | Description |
| :--- | :--- |
| `%inviterewards_leaderboard_name_1%` → `%_10%` | Player name at rank |
| `%inviterewards_leaderboard_points_1%` → `%_10%` | Points at rank |
| `%inviterewards_leaderboard_total_invites_1%` → `%_10%` | Total invites |


### Example Usage

```yaml
- "[message] &eTop 1: %inviterewards_leaderboard_name_1% (&a%inviterewards_leaderboard_points_1% points)"
- "[action_bar] &eYour rank: %inviterewards_player_leaderboard_rank%"
```

---

## 🆚 Free (Lite) vs Premium

| Feature | Free (Lite) 🆓 | Premium 💎 |
| :--- | :--- | :--- |
| Invite Types | Max 1 (default only) | Unlimited (Streamer, YouTuber, Custom, etc.) |
| Database | SQLite only | MySQL / MariaDB |
| Actions | Messages, Sounds, Commands | Particles, Titles, ActionBars, Extended Actions |
| Milestones | Max 3 tiers | Unlimited tiers |
| Anti-Alt System | Basic IP check | Advanced Anti-VPN |
| Leaderboards | Chat-based only | Full GUI + PlaceholderAPI support |

---

## 💎 Premium Features

- Persistent MySQL leaderboard
- Leaderboard GUI
- Extended action system (titles, menus, visuals)
- Async-safe database operations
- Advanced anti-fraud validation
- Leaderboard placeholder system

---

## 🚧 Premium Roadmap

- ✨ Particle effects system
- 📊 GUI leaderboard
- 🛡️ Anti-VPN detection

---

## 🛠️ Commands

### 👤 User Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/invitation code <type>` | Generate or view your invite code | `inviterewards.command.invitation` |
| `/invitation claim <type> <code>` | Claim an invite code | `inviterewards.command.invitation` |
| `/invitation leaderboard` | View top inviters | `inviterewards.command.invitation` |
| `/invitation pending` | View pending rewards | `inviterewards.command.invitation` |


### 🛡️ Admin Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/inviterewards help` | Show help menu | `inviterewards.command.admin` |
| `/inviterewards reload <config\|lang\|menu\|all>` | Reload plugin files | `inviterewards.command.admin` |
| `/inviterewards data info <player>` | View player data | `inviterewards.command.admin` |
| `/inviterewards data reset rewards <player>` | Clear pending rewards | `inviterewards.command.admin` |
| `/inviterewards data reset player <player>` | Reset all player data | `inviterewards.command.admin` |


### 💎 Premium Commands

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/inviterewards code create <type> <player> <code>` | Create manual invite code | `inviterewards.command.admin` |
| `/inviterewards code delete <type> <code>` | Delete invite code | `inviterewards.command.admin` |
| `/inviterewards code list <player>` | List player codes | `inviterewards.command.admin` |

---

## 🏗️ Architecture

### Core
- Invite system
- YAML / SQLite storage
- Placeholder engine
- Basic action system

### Premium
- MySQL/MariaDB
- Async processing
- GUI framework
- Extended actions
- Persistent data
- Leaderboard engine

---

## 👨‍💻 Developer API

```kotlin
val api = InviteRewardsAPI.get()
val playerPoints = api.getPlayerPoints(uuid)
```

---

## 📦 Installation

1. Download the `.jar`
2. Place it in `/plugins`
3. Restart server
4. Configure files

---

## 📄 License

MIT License

---

## ❤️ Credits

Developed by **IrvingLink**  
https://github.com/IrvingLink
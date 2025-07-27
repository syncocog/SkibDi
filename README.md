# VoidlightEvent Plugin

A comprehensive PvP event plugin for Spigot 1.21.4 that supports 1v1 to 4v4 battles with automatic queue management, admin-controlled matches, and spectator features.

## 🎯 Features

### ✅ Core Functionality
- **Auto-Queue System**: All players are automatically added to the event queue on join
- **Admin-Only Match Control**: Only admins can start and manage matches
- **Team-Based Combat**: Players are automatically split into Red and Blue teams
- **Beast Kit System**: Unbreakable diamond armor with Protection III and diamond sword with Unbreaking X
- **Spectator Mode**: Non-participating players become spectators with teleportation abilities
- **Real-time Scoreboard**: RGB/HEX color support showing match status, team sizes, and player roles
- **Match Logging**: All matches are logged to MySQL database for statistics
- **Countdown System**: Configurable countdown before matches begin
- **Automatic Victory Detection**: Matches end when one team is eliminated

### 🔧 Technical Features
- **MySQL Integration**: Uses HikariCP connection pooling for efficient database operations
- **Async Operations**: All database operations are performed asynchronously
- **Color Support**: RGB/HEX colors supported using legacy color codes
- **Inventory Protection**: Players cannot modify their kit during matches
- **Location Management**: Configurable spawn points for teams and lobby

## 📋 Requirements

- **Minecraft Version**: 1.21.4
- **Server Software**: Spigot/Paper 1.21.4
- **Java Version**: 17 or higher
- **Database**: MySQL 5.7+ or MariaDB 10.3+

## 🚀 Installation

### 1. Download and Install
1. Download `VoidlightEvent-1.0.0.jar` from the releases
2. Place the JAR file in your server's `plugins/` directory
3. Start your server to generate configuration files
4. Stop your server

### 2. Database Setup
Create a MySQL database and user for the plugin:

```sql
CREATE DATABASE voidlight_event;
CREATE USER 'voidlight'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON voidlight_event.* TO 'voidlight'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration
Edit the configuration files in `plugins/VoidlightEvent/`:

#### config.yml
```yaml
database:
  host: "localhost"
  port: 3306
  database: "voidlight_event"
  username: "voidlight"
  password: "your_password"
  pool-size: 10

event:
  countdown-duration: 10  # seconds
  max-players: 8
  min-players: 2

# Configure spawn locations (world coordinates)
spawn:
  lobby:
    world: "world"
    x: 0.5
    y: 64.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0
    
  red-spawns:
    - world: "world"
      x: 100.5
      y: 64.0
      z: 0.5
      yaw: 90.0
      pitch: 0.0
    # Add more spawn points as needed
    
  blue-spawns:
    - world: "world"
      x: -100.5
      y: 64.0
      z: 0.5
      yaw: -90.0
      pitch: 0.0
    # Add more spawn points as needed
```

#### messages.yml
Customize all plugin messages with color codes:
```yaml
# Example messages (full file is generated automatically)
match-starting: "<green>Match starting! Get ready!"
victory-red: "<red>🎉 RED TEAM WINS! 🎉"
victory-blue: "<blue>🎉 BLUE TEAM WINS! 🎉"
```

### 4. Start Server
Start your server. The plugin will automatically create the necessary database tables.

## 📝 Commands

### Admin Commands
All admin commands require the `voidlight.event.admin` permission (default: OP).

| Command | Description | Usage |
|---------|-------------|-------|
| `/event start <player1> <player2> [player3...player8]` | Start a match with 2-8 players | `/event start Steve Alex Bob Jane` |
| `/event cancel` | Cancel the current match | `/event cancel` |
| `/event spectate [player]` | Teleport to a specific player (spectators only) | `/event spectate Steve` |

### Examples
```bash
# Start a 1v1 match
/event start Player1 Player2

# Start a 2v2 match  
/event start Player1 Player2 Player3 Player4

# Start a 3v3 match
/event start Player1 Player2 Player3 Player4 Player5 Player6

# Cancel ongoing match
/event cancel

# Spectate a player
/event spectate Player1
```

## 🎮 How It Works

### Player Join Process
1. **Automatic Queue**: When a player joins the server, they are automatically added to the event queue
2. **Queue Display**: Players can see their queue status and total players in queue via scoreboard

### Match Flow
1. **Admin Selection**: Admin selects 2-8 players using `/event start`
2. **Team Assignment**: Players are automatically split into Red and Blue teams
3. **Preparation**: All other players become spectators
4. **Kit Distribution**: Selected players receive the "Beast" kit
5. **Teleportation**: Players are teleported to their team spawn points
6. **Countdown**: Configurable countdown begins (default: 10 seconds)
7. **Fight**: Match begins when countdown reaches zero
8. **Victory**: Match ends when all players of one team are eliminated
9. **Restoration**: All players are restored to original state and returned to lobby

### Beast Kit Contents
- **Helmet**: Diamond Helmet with Protection III (Unbreakable)
- **Chestplate**: Diamond Chestplate with Protection III (Unbreakable)
- **Leggings**: Diamond Leggings with Protection III (Unbreakable)
- **Boots**: Diamond Boots with Protection III (Unbreakable)
- **Weapon**: Diamond Sword with Unbreaking X (Unbreakable)

### Spectator Features
- **Invisible to Fighters**: Spectators are vanished from match participants
- **Free Movement**: Spectators can fly and move freely
- **Player Teleportation**: Click on players in chat or use `/event spectate <player>`
- **Real-time Updates**: Live scoreboard showing match progress

## 🎨 Scoreboard

The plugin displays a real-time scoreboard to all players showing:

### During Match
- **Match Status**: Current state (Countdown, In Progress, Ended)
- **Team Information**: 
  - Red Team: Alive count / Total count
  - Blue Team: Alive count / Total count
- **Player Role**: Fighter (Red/Blue) or Spectator
- **Server Info**: Online player count

### No Active Match
- **Status**: Waiting for match
- **Queue Information**: Number of players in queue
- **Server Info**: Online player count

## 🗄️ Database Schema

The plugin automatically creates these tables:

### matches
- `id` (Primary Key)
- `start_time` (Timestamp)
- `end_time` (Timestamp)
- `red_team` (JSON array of UUIDs)
- `blue_team` (JSON array of UUIDs)
- `winning_team` (RED/BLUE)
- `duration` (Match length in seconds)

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `voidlight.event.admin` | Access to admin commands | OP |
| `voidlight.event.spectate` | Allows spectating events | All players |

## ⚙️ Configuration Tips

### Spawn Point Setup
1. Go to your desired spawn locations
2. Use `/tp ~ ~ ~` to get exact coordinates
3. Add coordinates to `config.yml`
4. Test spawn points with `/event start` commands

### Performance Optimization
- **Database Pool Size**: Adjust `database.pool-size` based on server load
- **Update Frequency**: Scoreboard updates every second by default
- **Async Operations**: All database operations are non-blocking

## 🐛 Troubleshooting

### Common Issues

**Plugin won't start**
- Check database connection settings
- Verify MySQL user has correct permissions
- Check server logs for detailed error messages

**Players not receiving kits**
- Ensure players have empty inventory slots
- Check for conflicting plugins that modify inventories

**Spawn points not working**
- Verify world names in config.yml match exactly
- Ensure coordinates are valid (not in walls/void)
- Check Y-coordinate is above ground level

**Scoreboard not updating**
- Restart the plugin: `/reload confirm`
- Check for conflicting scoreboard plugins

### Debug Mode
Enable debug logging by adding to your server startup flags:
```bash
-Dvoidlight.debug=true
```

## 🤝 Support

For support, feature requests, or bug reports:
1. Check the troubleshooting section above
2. Review server console logs for error messages
3. Create an issue with detailed information about your setup

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Made with ❤️ for the Minecraft community**
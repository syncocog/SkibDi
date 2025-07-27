# Quick Installation Guide

## Prerequisites
- Spigot/Paper 1.21.4 server
- MySQL 5.7+ or MariaDB 10.3+
- Java 17+

## Step-by-Step Installation

### 1. Download Plugin
- Download `VoidlightEvent-1.0.0.jar` (4.3MB)
- Place in your server's `plugins/` folder

### 2. Start Server Once
```bash
# Start your server to generate config files
java -jar spigot-1.21.4.jar
# Stop the server after it fully loads
```

### 3. Database Setup
Run the provided SQL script:
```bash
mysql -u root -p < database-setup.sql
```

Or manually create the database:
```sql
CREATE DATABASE voidlight_event;
CREATE USER 'voidlight'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON voidlight_event.* TO 'voidlight'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Configure Plugin
Edit `plugins/VoidlightEvent/config.yml`:
```yaml
database:
  host: "localhost"
  port: 3306
  database: "voidlight_event"
  username: "voidlight"
  password: "your_secure_password"  # Use the password you set

spawn:
  lobby:
    world: "world"
    x: 0.5
    y: 64.0
    z: 0.5
    yaw: 0.0
    pitch: 0.0
```

### 5. Set Spawn Points
1. Go to your desired Red team spawn location
2. Run `/tp ~ ~ ~` to get coordinates
3. Add to `config.yml` under `red-spawns`
4. Repeat for Blue team spawns

Example:
```yaml
red-spawns:
  - world: "world"
    x: 100.5
    y: 64.0
    z: 0.5
    yaw: 90.0
    pitch: 0.0

blue-spawns:
  - world: "world"
    x: -100.5
    y: 64.0
    z: 0.5
    yaw: -90.0
    pitch: 0.0
```

### 6. Start Server
```bash
java -jar spigot-1.21.4.jar
```

## Testing
1. Join your server with two players
2. Run `/event start Player1 Player2`
3. Verify the match starts correctly

## Common Issues
- **Database connection failed**: Check credentials in config.yml
- **Players spawn in wrong location**: Verify spawn coordinates
- **Kit not working**: Ensure players have empty inventory slots

## Support
Check the main README.md for detailed troubleshooting and configuration options.
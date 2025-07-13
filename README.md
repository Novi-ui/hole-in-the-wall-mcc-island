# Hole in the Wall - MCC Style Plugin

A comprehensive Minecraft plugin that recreates the popular "Hole in the Wall" minigame from Minecraft Championship (MCC). Players must navigate through holes in moving walls to survive as long as possible.

## Features

### Core Gameplay
- **Multiplayer Minigame**: Support for 2-20 players per game
- **Progressive Difficulty**: Walls get faster and patterns more complex over time
- **Multiple Wall Patterns**: Basic, advanced, and expert patterns with mathematical algorithms
- **Special Game Modes**: Lightning mode, double walls, and chaotic mode
- **Real-time Collision Detection**: Precise hitbox detection with player poses (standing, crouching, prone)

### Game Phases
1. **Pre-Lobby**: Tutorial, countdown, interactive chat messages
2. **Main Phase**: Wall generation, movement, and elimination
3. **Post-Game**: Results, rewards, statistics display

### Player Management
- **Inventory Handling**: Automatic save/restore with safety checks
- **Pose Detection**: Standing, crouching, and prone positions
- **Movement Validation**: Anti-cheat and boundary checking
- **Statistics Tracking**: Comprehensive player data and achievements

### Scoring & Rewards
- **Dynamic Scoring**: Base points, streaks, risk bonuses, clutch bonuses
- **Economy Integration**: Vault support for currency rewards
- **Experience Rewards**: Minecraft XP distribution
- **Daily Bonuses**: First-win bonuses and multipliers

### Arena System
- **Multi-Arena Support**: Create and manage multiple game arenas
- **Flexible Boundaries**: Configurable platform and safe zone sizes
- **Wall Spawn Points**: Dynamic wall generation from multiple directions
- **Arena Validation**: Automatic checks for world loading and spawn points

### Effects & Feedback
- **Visual Effects**: Particles for collisions, warnings, and celebrations
- **Sound System**: Comprehensive audio feedback for all game events
- **Knockback Effects**: Realistic collision physics
- **Warning Systems**: Wall approach notifications

### Integration Support
- **Vault**: Economy system integration
- **Citizens**: NPC support for tutorials and statistics
- **HolographicDisplays**: Leaderboards and stats display
- **PlaceholderAPI**: Custom placeholders for other plugins
- **WorldEdit**: Arena creation and management

### Database & Statistics
- **SQLite/MySQL Support**: Flexible database backend
- **Player Statistics**: Games played, wins, best scores, achievements
- **Game History**: Complete match records and participation tracking
- **Leaderboards**: Top players by various metrics

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin using `/hitwadmin` commands
5. Create your first arena with `/hitwadmin create <name>`

## Commands

### Player Commands
- `/hitw join` - Join an available game
- `/hitw leave` - Leave current game
- `/hitw stats` - View your statistics
- `/hitw info` - Show plugin information
- `/hitw help` - Display help message

### Admin Commands
- `/hitwadmin reload` - Reload configuration
- `/hitwadmin create <name>` - Create arena at your location
- `/hitwadmin delete <name>` - Delete an arena
- `/hitwadmin list` - List all arenas
- `/hitwadmin start [arena]` - Start a new game
- `/hitwadmin stop` - Stop all active games
- `/hitwadmin forcestart` - Force start current game
- `/hitwadmin stats` - Show server statistics

## Permissions

- `hitw.use` - Basic plugin usage (default: true)
- `hitw.join` - Join games (default: true)
- `hitw.stats` - View statistics (default: true)
- `hitw.admin` - Administrative commands (default: op)
- `hitw.bypass` - Bypass game restrictions (default: op)

## Configuration

The plugin includes a comprehensive `config.yml` with over 100 configuration options:

### Game Settings
\`\`\`yaml
game:
  min-players: 2
  max-players: 20
  auto-start: true
  countdown-time: 30
  max-duration: 300
\`\`\`

### Wall Generation
\`\`\`yaml
walls:
  patterns:
    basic: ["simple_cross", "single_hole", "double_hole"]
    advanced: ["zigzag", "spiral", "random_holes"]
    expert: ["complex_maze", "shifting_holes"]
  movement:
    base-speed: 2.0
    speed-increment: 0.2
    max-speed: 8.0
\`\`\`

### Difficulty Progression
\`\`\`yaml
difficulty:
  progressive: true
  special-modes:
    lightning:
      activate-round: 10
      speed-multiplier: 2.0
    double:
      activate-round: 15
    chaotic:
      activate-round: 20
\`\`\`

## API Usage

The plugin provides a comprehensive API for developers:

\`\`\`java
// Get the plugin instance
HoleInTheWallPlugin plugin = HoleInTheWallPlugin.getInstance();

// Access managers
GameManager gameManager = plugin.getGameManager();
ArenaManager arenaManager = plugin.getArenaManager();
PlayerManager playerManager = plugin.getPlayerManager();

// Create a game
Arena arena = arenaManager.getArena("default");
Game game = gameManager.createGame(arena);

// Add player to game
game.addPlayer(player);
\`\`\`

## Technical Details

### Architecture
- **Modular Design**: Separated into logical packages (arena, game, player, wall, etc.)
- **Event-Driven**: Bukkit event system for player interactions
- **Async Processing**: Database operations and heavy computations
- **Thread-Safe**: Concurrent collections and proper synchronization

### Performance Optimizations
- **Chunk Preloading**: Automatic chunk loading around arenas
- **Pattern Caching**: Wall patterns cached for reuse
- **Batch Operations**: Database operations batched for efficiency
- **Particle Limiting**: Configurable particle limits for performance

### Compatibility
- **Minecraft Versions**: 1.19.4 to 1.21.4
- **Server Software**: Spigot, Paper, Purpur
- **Java Version**: 17+
- **Memory Requirements**: Minimum 1GB RAM recommended

## Development

### Building from Source
\`\`\`bash
git clone <repository-url>
cd hole-in-the-wall-plugin
mvn clean package
\`\`\`

### Project Structure
\`\`\`
src/main/java/com/hitw/
├── HoleInTheWallPlugin.java     # Main plugin class
├── arena/                       # Arena management
├── commands/                    # Command handlers
├── config/                      # Configuration management
├── effects/                     # Visual and sound effects
├── game/                        # Core game logic
├── integration/                 # External plugin integration
├── player/                      # Player management
├── score/                       # Scoring and rewards
├── utils/                       # Utility classes
└── wall/                        # Wall generation and movement
\`\`\`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Support

For support, bug reports, or feature requests:
- Create an issue on GitHub
- Join our Discord server
- Check the wiki for documentation

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Changelog

### Version 1.0.0
- Initial release
- Complete MCC-style gameplay implementation
- Multi-arena support
- Comprehensive configuration system
- Database integration
- External plugin integrations
- Full API support

## Credits

- Inspired by Minecraft Championship (MCC)
- Built for the Minecraft community
- Special thanks to all contributors and testers

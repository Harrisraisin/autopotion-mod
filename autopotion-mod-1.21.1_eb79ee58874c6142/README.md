# Auto Potion Mod for Minecraft 1.21.1

## Description
This Fabric mod automatically uses health potions from your hotbar when your health drops below 5 hearts (10 HP).

## Features
- Automatically detects and uses Instant Health potions (I and II) from hotbar
- Switches back to your sword after using a potion
- Toggle on/off with the R key (configurable)
- 1-second cooldown between potion uses
- Works with both regular and splash potions

## Installation
1. Install Fabric Loader for Minecraft 1.21.1
2. Install Fabric API
3. Place the mod JAR file in your mods folder
4. Launch Minecraft with the Fabric profile

## Usage
- Press R to toggle the mod on/off (you'll see a message in chat)
- Keep health potions in your hotbar
- The mod will automatically use them when your health drops below 5 hearts
- It will switch back to your sword after using a potion

## Building from Source
1. Clone or download this repository
2. Open a terminal in the mod directory
3. Run: `./gradlew build`
4. The compiled JAR will be in `build/libs/`

## Single-Player Only
This mod is designed for single-player use only. Using automation mods in multiplayer servers may violate server rules.

## License
MIT License
Test build trigger

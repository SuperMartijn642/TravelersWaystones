<div align="center">
<img width="160" src="assets/logo-256.png" alt="Traveler's Waystones logo">

# Traveler's Waystones

**Teleport between the places you've been!**

[![Modrinth](https://img.shields.io/modrinth/dt/travelers-waystones?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/mod/travelers-waystones)
[![CurseForge](https://img.shields.io/curseforge/dt/1814185?logo=curseforge&label=CurseForge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/travelers-waystones)
[![Discord](https://img.shields.io/badge/Discord-join-5865F2?logo=discord&logoColor=white)](https://discord.gg/QEbGyUYB2e)
</div>

**Traveler's Waystones** adds waystones which allow you to teleport between the places you've been. Discover waystones
on your travels and teleport between them through a screen where you can search your waystones, mark your favorites and
see where each waystone is. Waystones work at any distance and across dimensions, and the Warp Pendant
teleports you to your waystones from anywhere.

<img width="640" src="assets/discovering-and-teleporting.gif" alt="A player discovers a waystone, then opens the teleport screen at another waystone and teleports to it">

![Separator](assets/separator-deepslate-bricks-diamond.webp)

### Features
- Waystones in 19 different stone types
- Claim a waystone by giving it a name, other players discover it by right-clicking it
- A teleport screen with search, favorites and a 3D preview of each waystone
- The Warp Pendant opens the teleport screen from anywhere (right-click it or press `K`), also from a Curios or
  Trinkets slot
- Warnings before teleporting to a waystone without a safe spot around it
- Waystones are protected from accidental breaking and can be moved up to 10 blocks by their owner

For the full description and recipes, see the <img height="16" src="https://cdn.simpleicons.org/modrinth/00AF5C" alt=""> [Modrinth](https://modrinth.com/mod/travelers-waystones)
or <img height="16" src="https://cdn.simpleicons.org/curseforge/F16436" alt=""> [CurseForge](https://www.curseforge.com/minecraft/mc-mods/travelers-waystones) page.

![Separator](assets/separator-mud-bricks-diamond.webp)

### For Developers
See the [wiki](https://github.com/SuperMartijn642/TravelersWaystones/wiki) for how to add the API as a dependency and for
examples, such as a map integration. The API is in `com.supermartijn642.travelerswaystones.api`:
- `WaystoneType` lets you register your own blocks as waystones and choose where players arrive
- `WaystoneEvents` lets you allow or deny claiming, discovering, moving and teleporting, and listen for changes to
  waystones
- `GlobalWaystoneManager` and `PersonalWaystoneManager` give access to all waystones and to the ones each player has
  discovered, also on the client
- `DiscoveredWaystoneTarget#teleport` teleports a player and `PersonalWaystoneManager#openTeleportScreen` opens the
  teleport screen

The API is the same on every version, apart from Minecraft's own class renames. Missing something? Feel free to open an
issue request for it.

![Separator](assets/separator-polished-blackstone-bricks-diamond.webp)

### Issues and Suggestions
Found a bug or have a suggestion? Please open an [issue](https://github.com/SuperMartijn642/TravelersWaystones/issues).

For questions and discussion, feel free to join the SuperMartijn642 discord server!

[<img width="400" src="assets/discord.png" alt="SuperMartijn642 Discord server">](https://discord.gg/QEbGyUYB2e)

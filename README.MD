# Blur [![Build Status](https://ci.drtshock.net/buildStatus/icon?job=Blur)](https://ci.drtshock.net/job/Blur/) 
I am your Minecraft Game Engine. You come up with the ideas, and allow me to make them a reality.

As you might know, creating a game can be both time consuming and expensive. So wouldn't it be great if you could pay no money and little time and in return get your own unique game? Well Blur strives to achieve that for you.

Blur is a module-based Game Engine, defining functionality through blocks of variables that tells the engine what to do and how to do it.

Here is an example of a SurvivalGames gamemode map configuration [blur.yml](https://gist.github.com/SupaHam/286b137f4cfc17d9e549).

# Issues
Thanks to Atlassian, Blur uses JIRA for its issue tracking and management system. Before considering making a report please head over to our [issue
tracker](https://blurengine.atlassian.net/) to make a report.

# Documentation
Complete documentation and guidance can be found over at [https://blurengine.com](https://blurengine.com).

# Examples
Here are a few examples of the things you can do with Blur. Pick and choose your modules to create your perfect game!

### Lobby system for breaks in between games
##### blur.yml
```yaml
modules:
- MapLoader:
    maps: {file: Rotations}
- Lobby
- Extents:
  - id: default-spawns
    cylinder: {base: '0, 60, 0', radius: 15}
- Spawns
```
The preceding Blur config is a demonstration of a Lobby environment. There are a total of four Blur Modules used: `MapLoader`, `Lobby`, `Extents`, and `Spawns`.
- MapLoader: Loads maps (worlds) in a file called Rotations in the Minecraft server root directory.
- Lobby: A countdown of 15 seconds between each game and adds all players to the game automatically.
- Extents: Extent defintions in the map. Only one with the id `default-spawns` is created to eventually spawn players at `0, 60, 0` randomised in a 15 block radius.
- Spawns: Enables the spawn modules so that joining players can be teleported to the lobby at all times. By default, it will search for a `default-spawns` extent to use.

A more customised version:
##### blur.yml
```yaml
modules:
- MapLoader:
    archive: true # Archive sessions after they are finished
    maps:
      file: Rotations # Load session from a file in the root directory called Rotations
- Lobby:
    countdown: 5s # Countdown from 5 seconds before starting
    delay-start-session: 3s # Wait 3 seconds before teleporting to the session
    required-players: 4 # In order for MapLoader to be triggered, at least 4 players must be online.
- Extents:
  - id: spawns # Create a new extent with the id of 'spawns'
    cylinder: {base: '988, 12, 2045', radius: 5} # Construct a Cylinder at 988, 12, 2045 with the radius of 5 and height of 1
- Spawns:
    spawns:
    - spawns # Add the extent of id 'spawns' as a spawning extent
    # Alternatively:
    # Add the extent of id 'spawns' as a spawning extent, with spawns pointing towards 986.5, 11, 2046.5
    #- spawns: {point-to: '986.5, 11, 2046.5'}
```

### A game of Control Points
##### blur.yml
```yaml
modules:
- Teams:
  - {id: 'red', name: 'Red', max: 10, chat-prefix: '&c', color: 'Red'} # Red team with a soft maximum 10 slots.
  - {id: 'blue', max-overfill: 20, chat-prefix: '&9', color: 'Blue'} # Blue team with an absolute maximum of 20 slots.
- Goal:
    time-limit: 15m30s # Game lasts 15 minutes and 30 seconds
- BControlPoints:
    capture-time: 10s # All control points take 10 seconds to capture
    incremental: true # Capture progress is retained even after a team stops capturing the control point
    control-points:
    - id: center
      capture-time: ~-2s # Capture time is 2 seconds less than the default (defined above)
      capture: {cylinder: {base: '15, 76, -5.9', radius: 3, height: 3}} # Cylinder at 15, 76, -5.9 with a radius of 3 and height of 3
```

# Disclaimer
Blur is still in its alpha stage. Things can change around, things might break, but we can't be held responsible for that. Please help us develop Blur
out of the alpha stage by creating Pull Requests.

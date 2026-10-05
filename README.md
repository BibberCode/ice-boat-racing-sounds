# Ice Boat Racing Sounds

Fabric-Mod für Minecraft 1.21.4: Boote klingen wie Rennautos auf Eis.

## Verhalten

- **Einsteigen** → `engine_start` (einmalig) + Start des Motor-Loops
- **Fahren** → `engine_stand` als Loop; Pitch folgt der Geschwindigkeit, Volume folgt Gaspedal (W-Taste) und Untergrund (auf Eis volle Lautstärke, sonst leiser)
- **Aussteigen** → Loop stoppt + `engine_turnoff` (einmalig)

Gilt für alle Boote (`AbstractBoatEntity`, also auch Chest-Boote). Die Sounds sind clientseitig und nur für den Fahrer hörbar.

## Sounds austauschen

OGG-Dateien in `src/main/resources/assets/ice_boat_racing_sounds/sounds/` ersetzen, Namen in `sounds.json` lassen.

## Setup / Build

Fabric-Doku zum Einrichten: https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up

```bash
./gradlew build
```

## Lizenz

CC0-1.0 – siehe `LICENSE`.

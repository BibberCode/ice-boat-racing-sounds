# Ice Boat Racing Sounds

Fabric-Mod für Minecraft 1.21.4: Boote klingen wie Rennautos auf Eis.

## Verhalten (Racing-Dynamik, nur auf Eis)

Der Motor läuft **nur auf Eis** (Eis, Packed, Blau, Frosted) – mit Start-Stopp-Automatik und 0,5 s Entprellung an der Eiskante. Auf Wasser bleibt es still, dort paddelt man vanilla. Die Vanilla-Paddelgeräusche (`paddle_water`, `paddle_land`) sind auf Eis stummgeschaltet.

- **Eis berühren** → `engine_start`: Anlasser kurbelt, Zündung fängt, Drehzahl flackert hoch
- **Fahren** → 5 kurze Gänge (Schalten schon ab ~8 m/s): Drehzahl steigt pro Gang hörbar, beim Schalten (hoch wie runter) gibt es einen Sound mit kurzem Drehzahleinbruch; am Limit stottert der Begrenzer (Zündung setzt aus). Die Drehzahl atmet zusätzlich (Lastwechsel)
- **Schubknallen** → wer bei hoher Drehzahl vom Gas geht, bekommt 1–3 Auspuff-Pops (`engine_backfire`)
- **Motor-Layer** → `engine_loop_low` (tiefes Blubbern) + `engine_loop_high` (brüllendes Top-End) werden per Crossfade nach Drehzahl gemischt; Pitch und Volume folgen dem Gas (W-Taste)
- **Fahrtwind** → `wind_loop`, wächst quadratisch mit der Geschwindigkeit und ist böig (zufällige Stärke-Schwankungen)
- **Drift** → `skid_loop`: Eis-Kratzen plus Squeal, sobald das Boot seitlich rutscht
- **Aussteigen / Eis verlassen** → alle Layer stoppen + `engine_turnoff` (Drehzahl fällt, Stottern, aus)

Gilt für alle Boote (`AbstractBoatEntity`, also auch Chest-Boote). Die Sounds sind clientseitig und nur für den Fahrer hörbar. Alle OGGs sind selbst synthetisiert (`/tmp/opencode/synth_engine.py`, lizenzfrei) und nahtlos loopbar.

## Sounds austauschen

OGG-Dateien in `src/main/resources/assets/ice_boat_racing_sounds/sounds/` ersetzen, Namen in `sounds.json` lassen.

## Setup / Build

Fabric-Doku zum Einrichten: https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up

```bash
./gradlew build
```

## Lizenz

CC0-1.0 – siehe `LICENSE`.

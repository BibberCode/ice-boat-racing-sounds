# Ice Boat Racing Sounds

Fabric-Mod für Minecraft 1.21.4: Boote klingen wie Rennautos auf Eis.

**Wichtig: Der Mod verändert ausschließlich das Audioerlebnis.** Boot-Tempo,
Physik, Steuerung, Kollisionen, Items, Blöcke und alle Spielregeln bleiben
exakt Vanilla. „Gänge“, „RPM“, „Load“ oder „Drift“ sind intern simulierte
Werte fürs Sounddesign – sie beeinflussen niemals die tatsächliche Bewegung.

## Verhalten (Racing-Dynamik, nur auf Eis)

Der Motor läuft **nur auf Eis** (Eis, Packed, Blau, Frosted) – mit
Start-Stopp-Automatik und 0,5 s Entprellung an der Eiskante. Auf Wasser bleibt
es still, dort paddelt man vanilla. Die Vanilla-Paddelgeräusche
(`paddle_water`, `paddle_land`) sind auf Eis stummgeschaltet.

- **Eis berühren** → `engine_start`: Anlasser kurbelt, Zündung fängt, Drehzahl flackert hoch
- **Engine Load** → Vollgas + Beschleunigung klingt belastet/aggressiv (Turbo-Jaulen, hellere Layer), konstantes Tempo stabil, Gas weg ruhig mit Overrun-Charakter; Launch aus langsamem Tempo bekommt extra Schub
- **Gänge (5, simuliert)** → Schalten schon ab ~8 m/s, hoch wie runter; Hochschalten mit RPM-Drop + Schaltknall, Runterschalten mit Rev-Matching-Blip; am Limit stottert der Begrenzer (Zündung setzt aus)
- **Motor-Layer** → Low/Mid/High/Redline blenden weich über (−keine Klicks); Pitch/Volume folgen Gas, Load und Bootstyp (Chest etwas tiefer, Floß etwas heller)
- **Turbo/Intake** → dezentes Jaulen unter Last, Blow-off-Zischen bei hartem Gaswegnehmen aus hoher Drehzahl
- **Schubknallen** → nach hoher Last + Gaswegnehmen 1–3 Pops in Stufen, mit Cooldown
- **Fahrtwind** → wächst mit Tempo, böig, plus Umgebungs-Offenheit (Tunnel trägt leicht)
- **Drift/Slide** → Kratzen + Squeal in Stufen nach seitlicher Bewegung, mit Eis-Sorten-Charakter (Blau heller, Frosted weicher)
- **Kollision/Landung** → leichte/starke Impacts bei Vanilla-Kollisionen (beobachteter Tempoverlust, Cooldown), kurzer Landing-Sound nach Flugphase (nicht über Wasser)
- **Fremde Boote** → bis zu 3 Boote in der Nähe sind leise hörbar (Entfernung → Lautstärke, dezenter Doppler)
- **Aussteigen / Eis verlassen** → weiches Ausblenden statt hartem Cut + `engine_turnoff`

Gilt für alle Boote und Flöße (`AbstractBoatEntity`: Boote, Chest-Boote, Rafts,
Chest-Rafts). Alles clientseitig. Alle OGGs sind selbst synthetisiert
(lizenzfrei) und nahtlos loopbar.

## Konfiguration

`config/ice-boat-racing-sounds.json` (wird beim ersten Start angelegt):

- Lautstärken: engine, wind, skid, turbo, backfire, shift, effects, nearby
- Schalter: doppler, surfaceAudio, nearbyBoats, debugAudio (Audio-Telemetrie ins Log, kein HUD)

Alle Einstellungen wirken nur auf Audio.

## Sounds austauschen

OGG-Dateien in `src/main/resources/assets/ice_boat_racing_sounds/sounds/` ersetzen, Namen in `sounds.json` lassen.

## Setup / Build

Fabric-Doku zum Einrichten: https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up

```bash
./gradlew build
```

## Lizenz

CC0-1.0 – siehe `LICENSE`.

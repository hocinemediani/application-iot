# PLAN - Ghost Radar V1

## Reprise rapide

- **Dernière étape validée** : Cadrage complet des spécifications (51/51 questions résolues).
- **Prochaine action immédiate** : Écriture des tests unitaires JUnit du Jalon 1 (`domain`) avant toute implémentation.

> Détail de l'étape validée : `SPEC.md` intégré, `PLAN.md` créé, section de continuité ajoutée à `AGENTS.md`, entrée de cadrage ajoutée à `JOURNAL.md`. Aucun code métier n'a encore été écrit.

## Règles de suivi

- Cocher `- [x]` uniquement les sous-tâches réellement terminées et vérifiées.
- À chaque fin d'intervention : mettre à jour `Dernière étape validée`, `Prochaine action immédiate`, puis ajouter une entrée datée dans `JOURNAL.md`.
- Ne jamais démarrer l'implémentation d'une classe avant ses tests JUnit correspondants (règle TDD).

---

## Jalon 1 — Domaine pur testable (Java pur, aucun `android.*`)

### Cadrage & spécifications

- [x] Lire `AGENTS.md`, `SPEC.md` et `app/build.gradle.kts`.
- [x] Poser les questions de seuils, règles et ambiguïtés avant tout code.
- [x] Intégrer les 51 réponses et décisions techniques dans `SPEC.md`.
- [x] Créer `PLAN.md` avec le suivi inter-sessions.
- [x] Ajouter la section `## Continuité inter-sessions (obligatoire)` dans `AGENTS.md`.
- [x] Ajouter l'entrée de cadrage dans `JOURNAL.md`.
- [x] Figer les constantes par défaut dans `SPEC.md` (section 6).

### Tests JUnit à écrire AVANT le code

- [ ] `GeoMathTest` : Haversine (`R = 6 371 000 m`), bearing, destination, normalisation d'angle, point-dans-polygone, distance point-segment.
- [ ] `GeoPolygonTest` : validation (≥ 3 sommets, auto-intersection, doublons < 0,5 m), `contains`, point le plus proche.
- [ ] `PlayAreaTest` : polygone extérieur + obstacles, `contains`, clamp/glissement le long des bords extérieurs et intérieurs.
- [ ] `HauntedZoneTest` : zone polygonale aléatoire, cercle admin, `contains`.
- [ ] `GameConfigTest` : valeurs par défaut et surcharge de chaque constante.
- [ ] `GameInputTest` : `floor = 0`, `pressureHpa` nullable, snapshot immuable.
- [ ] `GameSessionTest` : transitions `EXPLORATION → TRACKING → SCREAMER → CAPTURE → RECOMPENSE`, cooldown 30 s, pause hors zone.
- [ ] `GhostSpawnerTest` : probabilité 2 %/s, spawn 15–40 m, spawn admin au point carte.
- [ ] `GhostMovementTest` : vitesses 1,2 / 2,4 m/s, clamp + glissement extérieur/obstacles, appât, stun.
- [ ] `NoiseMonitorTest` : seuil -30 dBFS, fenêtre 200 ms, hystérésis 2 s, alerte maintenue 3 s.
- [ ] `CaptureControllerTest` : distance < 4 m, 5 s cumulées, pause si verrou perdu, tolérance ±12°, timeout 30 s, fantôme ancré, capture pendant stun.
- [ ] `EctoplasmMineTest` : rayon 3 m, stun 10 s, one-shot, max 3, visibilité carte, événement d'alerte.
- [ ] `LureTest` : durée 8 s, vitesse 1,5 m/s, priorité, one-shot, sans effet si stun, clamp.
- [ ] `InventoryTest` : stocks initiaux 1 mine / 1 appât, ajout/consommation.
- [ ] `BeamProfileTest` : 5 niveaux, réduction de temps, augmentation de tolérance, minimum 2 s.
- [ ] `FloorProviderTest` : retourne 0 en V1.

### Classes à créer dans `com.example.ghostradar.domain`

- [ ] `domain.geo.LatLon`
- [ ] `domain.geo.GeoMath`
- [ ] `domain.geo.GeoPolygon`
- [ ] `domain.geo.MapProjection`
- [ ] `domain.geo.PlayArea` (polygone extérieur + obstacles)
- [ ] `domain.geo.HauntedZone` (polygone aléatoire / cercle admin)
- [ ] `domain.config.GameConfig` (+ `Builder` imbriqué)
- [ ] `domain.game.GameState`
- [ ] `domain.game.GameInput` (`int floor = 0`, `Double pressureHpa` nullable)
- [ ] `domain.game.GameSnapshot`
- [ ] `domain.game.GameEvent`
- [ ] `domain.game.GameEventType`
- [ ] `domain.game.GameListener`
- [ ] `domain.game.GameSession`
- [ ] `domain.game.GameClock`
- [ ] `domain.game.RandomSource`
- [ ] `domain.game.GhostSpawner`
- [ ] `domain.game.FloorProvider` (méthode par défaut retournant 0 en V1)
- [ ] `domain.entity.Ghost`
- [ ] `domain.entity.GhostState`
- [ ] `domain.ai.GhostMovement`
- [ ] `domain.ai.NoiseMonitor`
- [ ] `domain.ai.CaptureController`
- [ ] `domain.item.Gadget`
- [ ] `domain.item.GadgetType`
- [ ] `domain.item.EctoplasmMine`
- [ ] `domain.item.Lure`
- [ ] `domain.item.Inventory`
- [ ] `domain.item.BeamProfile`

> Contrainte : ne pas créer `WorldMap`, `FloorMap` ni `Floor` en J1. `FloorProvider` est la seule abstraction d'étage.

### Vérifications J1

- [ ] Aucun import `android.*` dans `domain`.
- [ ] `./gradlew test` vert.
- [ ] Mettre à jour `PLAN.md` et `JOURNAL.md`.

---

## Jalon 2 — Capteurs Android & écran caméra

### Dépendances & capteurs

- [ ] Ajouter CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) — dépendance pré-approuvée.
- [ ] Ajouter Play Services Location — dépendance pré-approuvée.
- [ ] Créer les interfaces capteurs dans `com.example.ghostradar.sensors`.
- [ ] Implémenter `GpsSensor` avec `FusedLocationProviderClient` (accuracy ≤ 20 m, 1 Hz, 1 m).
- [ ] Implémenter `CompassSensor` (`TYPE_ROTATION_VECTOR`, fallback magnétomètre, low-pass 0,15).
- [ ] Implémenter `MicrophoneSensor` (`AudioRecord`, RMS → dBFS, fenêtre 200 ms).
- [ ] Implémenter `AccelerometerSensor` (`TYPE_ACCELEROMETER`, 50 Hz).
- [ ] Gérer les permissions runtime (localisation, micro).
- [ ] Adapter les capteurs vers `GameInput` au tick fixe de 100 ms.

### Caméra & HUD

- [ ] Intégrer `CameraX PreviewView` en plein écran.
- [ ] Ajouter le filtre bleu/IR (alpha 0,15).
- [ ] Ajouter les hooks de glitch (500 ms) à l'apparition et à la capture.
- [ ] HUD `● REC` clignotant + horloge `HH:MM:SS`.
- [ ] Jauge de bruit discrète (-60 à 0 dBFS).
- [ ] Verrouiller le portrait et gérer la pause en arrière-plan.

### Vérifications J2

- [ ] Tests unitaires des adaptateurs purs (sans Android) si possible.
- [ ] `./gradlew test` vert.
- [ ] `./gradlew assembleDebug` OK.
- [ ] Mettre à jour `PLAN.md` et `JOURNAL.md`.

---

## Jalon 3 — Radar, carte & boucle de jeu visuelle

### Radar & carte

- [ ] Radar `Canvas` : portée 100 m, orientation `phone-up`, distance chiffrée, 360°.
- [ ] Radar visible uniquement en `TRACKING`.
- [ ] Carte `Canvas` : projection ENU, polygone extérieur, obstacles, zones hantées, joueur, mines.
- [ ] Mode cartographie : enregistrement GPS tous les 3 m / 2 s.
- [ ] Simplification Douglas-Peucker à 1 m à l'arrêt.
- [ ] Édition des sommets : déplacer, ajouter, supprimer, undo.
- [ ] Tracé manuel des polygones d'obstacles fermés.
- [ ] Admin : zone hantée circulaire avec rayon configurable.
- [ ] Admin : spawn au point renseigné sur la carte, ignore zones/probabilité, debug uniquement.

### Combat & gadgets

- [ ] Écran Screamer : délai aléatoire 1–2 s, durée 2 s, vibration, non skippable.
- [ ] Écran Capture : tilt accéléromètre, vecteurs de force aléatoires, verrouillage ±12°.
- [ ] Temps cumulé 5 s, pause si verrou perdu, timeout 30 s.
- [ ] Position GPS du fantôme ancrée pendant la capture.
- [ ] Pose des mines (GPS, dans le polygone, distance min 5 m, max 3).
- [ ] Affichage des mines sur la carte et alerte de stun.
- [ ] Pose/utilisation des appâts (8 s, 1,5 m/s, priorité, clamp).
- [ ] Capture possible pendant le stun.
- [ ] Sons et vibrations système, constantes configurables.

### Vérifications J3

- [ ] Tests domaine associés verts.
- [ ] `./gradlew test` vert.
- [ ] `./gradlew assembleDebug` OK.
- [ ] Mettre à jour `PLAN.md` et `JOURNAL.md`.

---

## Jalon 4 — Progression, Fantômedex, persistance & polish

### Persistance

- [ ] Ajouter Gson (`com.google.code.gson:gson`) — dépendance autorisée.
- [ ] Créer l'interface `GameRepository` dans `domain`.
- [ ] Créer `GameSave` (polygone extérieur, obstacles, zones, inventaire, beam, Fantômedex, mines).
- [ ] Implémenter le stockage `SharedPreferences` côté Android derrière `GameRepository`.
- [ ] Persister toutes les données sauf l'état de session en cours.

### Progression

- [ ] `LootTable` modulaire (45 % mines / 45 % appâts / 10 % beam).
- [ ] `PitySystem` configurable (activation, seuil, garantie).
- [ ] Ouverture de Lootbox et intégration à l'inventaire.
- [ ] Table des niveaux de faisceau (5 niveaux, temps et tolérance).
- [ ] Application des améliorations au `CaptureController`.
- [ ] Fantômedex : 10 identités chargées depuis un JSON fourni.
- [ ] Mini-jeu d'identification : 1–10 Hz, tolérance ±0,5 Hz, 3 essais, réessai.
- [ ] Identité révélée purement cosmétique.

### Finitions

- [ ] Équilibrage des vitesses, seuils et probabilités.
- [ ] Finitions visuelles et audio.
- [ ] Tests complets et régression.
- [ ] `./gradlew test` vert.
- [ ] `./gradlew assembleDebug` OK.
- [ ] Mettre à jour `PLAN.md` et `JOURNAL.md`.

---

## Hors V1 — Backlog V2

- [ ] Baromètre `TYPE_PRESSURE` et calibrage `P0`.
- [ ] Détection d'étage : 0,36 hPa/étage, hystérésis ±0,1 hPa.
- [ ] `FloorProvider` basé sur la pression.
- [ ] Zones hantées et spawn multi-étages.
- [ ] Filtrage des fantômes par étage.
- [ ] Lissage GPS avancé (moyenne glissante/Kalman).

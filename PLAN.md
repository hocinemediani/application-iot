# PLAN - Ghost Radar V1

## Reprise rapide

- **Dernière étape validée** : `MapProjection` implémenté et validé — origine au centre de la boîte englobante du polygone extérieur, projection ENU en mètres, projection inverse et validation des entrées.
- **État J1** : **terminé** — le domaine pur V1 est complet et testé. `GameEvent`, `GameEventType`, `GameListener` et `Gadget` sont explicitement reportés hors J1.
- **Prochaine action immédiate** : Démarrer le Jalon 2 — ajouter CameraX et Play Services Location via le Version Catalog, puis créer les interfaces capteurs dans `com.example.ghostradar.sensors`.

> Détail : `SPEC.md` intégré, `PLAN.md` créé, `AGENTS.md` actualisé, commande `/jalon1` créée. Le Jalon 1 est terminé : `LatLon`, `GeoMath`, `GeoPolygon`, `PlayArea`, `HauntedZone`, `RandomSource`, `GameState`, `GameConfig`, `GameInput`, `GameSnapshot`, `GameClock`, `GameSession`, `GhostSpawner`, `Ghost`, `GhostState`, `GhostMovement`, `NoiseMonitor`, `CaptureController`, `EctoplasmMine`, `Lure`, `GadgetType`, `Inventory`, `BeamProfile`, `FloorProvider` et `MapProjection` sont implémentés et testés.

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
- [x] Intégrer les règles de continuité inter-sessions dans `AGENTS.md`.
- [x] Ajouter l'entrée de cadrage dans `JOURNAL.md`.
- [x] Figer les constantes par défaut dans `SPEC.md` (section 6).
- [x] Créer la commande OpenCode `/jalon1` (`.opencode/commands/jalon1.md`).
- [x] Configurer le SDK local et vérifier `test` + `assembleDebug` sur le squelette.

### Tests JUnit à écrire AVANT le code

- [x] `GeoMathTest` : Haversine (`R = 6 371 000 m`), bearing, destination, normalisation d'angle, point-dans-polygone, distance point-segment.
- [x] `GeoPolygonTest` : validation (≥ 3 sommets, auto-intersection, doublons < 0,5 m), `contains`, point le plus proche.
- [x] `PlayAreaTest` : polygone extérieur + obstacles, `contains`, clamp/glissement le long des bords extérieurs et intérieurs.
- [x] `HauntedZoneTest` : zone polygonale aléatoire, cercle admin, `contains`.
- [x] `GameConfigTest` : valeurs par défaut et surcharge de chaque constante.
- [x] `GameInputTest` : `floor = 0`, `pressureHpa` nullable, snapshot immuable.
- [x] `GameSessionTest` : transitions `EXPLORATION → TRACKING → SCREAMER → CAPTURE → RECOMPENSE`, cooldown 30 s, pause hors zone.
- [x] `GhostSpawnerTest` : probabilité 2 %/s, spawn 15–40 m, spawn admin au point carte.
- [x] `GhostMovementTest` : vitesses 1,2 / 2,4 m/s, clamp + glissement extérieur/obstacles, appât, stun.
- [x] `NoiseMonitorTest` : seuil -30 dBFS, fenêtre 200 ms, hystérésis 2 s, alerte maintenue 3 s.
- [x] `CaptureControllerTest` : distance < 4 m, 5 s cumulées, pause si verrou perdu, tolérance ±12°, timeout 30 s, fantôme ancré, capture pendant stun.
- [x] `EctoplasmMineTest` : rayon 3 m, stun 10 s, one-shot, max 3, visibilité carte, événement d'alerte.
- [x] `LureTest` : durée 8 s, vitesse 1,5 m/s, priorité, one-shot, sans effet si stun, clamp.
- [x] `InventoryTest` : stocks initiaux 1 mine / 1 appât, ajout/consommation.
- [x] `BeamProfileTest` : 5 niveaux, réduction de temps, augmentation de tolérance, minimum 2 s.
- [x] `FloorProviderTest` : retourne 0 en V1.
- [x] `MapProjectionTest` : origine, axes ENU, projection inverse, rayon terrestre, validation.

### Classes à créer dans `com.example.ghostradar.domain`

- [x] `domain.geo.LatLon`
- [x] `domain.geo.GeoMath`
- [x] `domain.geo.GeoPolygon`
- [x] `domain.geo.MapProjection`
- [x] `domain.geo.PlayArea` (polygone extérieur + obstacles)
- [x] `domain.geo.HauntedZone` (polygone aléatoire / cercle admin)
- [x] `domain.config.GameConfig` (+ `Builder` imbriqué)
- [x] `domain.game.GameState`
- [x] `domain.game.GameInput` (`int floor = 0`, `Double pressureHpa` nullable)
- [x] `domain.game.GameSnapshot`
- [x] `domain.game.GameSession`
- [x] `domain.game.GameClock`
- [x] `domain.game.RandomSource` (interface + fabrique déterministe `seeded`)
- [x] `domain.game.GhostSpawner`
- [x] `domain.game.FloorProvider` (méthode par défaut retournant 0 en V1)
- [x] `domain.entity.Ghost`
- [x] `domain.entity.GhostState`
- [x] `domain.ai.GhostMovement`
- [x] `domain.ai.NoiseMonitor`
- [x] `domain.ai.CaptureController`
- [x] `domain.item.GadgetType`
- [x] `domain.item.EctoplasmMine`
- [x] `domain.item.Lure`
- [x] `domain.item.Inventory`
- [x] `domain.item.BeamProfile`

### Reportés hors J1 (non requis pour la clôture)

- `domain.game.GameEvent` — catalogue d'événements à spécifier.
- `domain.game.GameEventType` — catalogue d'événements à spécifier.
- `domain.game.GameListener` — catalogue d'événements à spécifier.
- `domain.item.Gadget` — interface non requise, `GadgetType` et `Inventory` couvrent le besoin V1.

> Décision : `GameEvent`, `GameEventType` et `GameListener` sont reportés à une étape dédiée ; `SPEC.md` ne définit pas leur catalogue. `GameSession` expose pour l'instant des transitions explicites et des getters.

> Décision : `Ghost` est mutable et porte son timer de stun (`stun`, `advanceStun`, `isStunned`) ; `GhostMovement.update(...)` décompte ce timer et gèle le déplacement de tout tick commencé stunné, le mouvement reprenant au tick suivant. L'appât est prioritaire sur l'alerte bruit et s'arrête exactement sur la position du joueur sans dépassement. Le clamp/glissement réutilise `PlayArea.clamp`.

> Décision : `NoiseMonitor` reçoit des blocs RMS normalisés avec leur durée et possède la fenêtre d'énergie de 200 ms, la conversion dBFS et le lissage. La durée d'hystérésis est un délai d'armement continu au-dessus du seuil ; après extinction, ce délai est de nouveau requis. Le monitor ignore la phase de jeu, l'intégration neutralise l'alerte pendant `CAPTURE` via `NOISE_AFFECTS_CAPTURE`.

> Décision : `CaptureController.start(playerPosition, ghost)` exige une distance strictement inférieure à 4 m, puis le contrôleur consomme `(aimErrorDeg, beamActive, deltaSeconds)`. Le verrou est `beamActive && |erreur| <= 12°`, sans hystérésis. Le timeout de 30 s couvre tout le combat, verrou perdu compris ; le succès est évalué avant le timeout. Le fantôme est réancré à chaque tick et le stun ne bloque pas la capture. `BeamProfile` et les vecteurs de force seront intégrés à des étapes dédiées.

> Décision : `EctoplasmMine(GameConfig, PlayArea)` gère le champ de mines et expose des `PlacedMine` imbriquées. Le placement exige une position jouable, moins de `MINE_MAX_ACTIVE` mines non déclenchées et au moins `MINE_MIN_DISTANCE_M` de chacune. Au plus une mine se déclenche par tick (la plus proche à moins de `MINE_TRIGGER_RADIUS_M`) ; `update` renvoie `true` pour signaler l'alerte. Une mine déclenchée reste visible tant que le fantôme est stunné. Les flags `MINE_ONE_SHOT` et `MINE_PERSIST_UNTIL_TRIGGERED` ne créent pas de variante non spécifiée : le comportement reste one-shot et persistant jusqu'au déclenchement.

> Décision : `Lure` ne porte que l'activation, la durée et le one-shot ; `GhostMovement` conserve l'attraction, la priorité et le clamp. L'activation est refusée sans consommation sur un fantôme stunné. Une fois actif, le timer continue pendant un stun ultérieur ; l'objet reste consommé après expiration.

> Décision : `GadgetType` limite les consommables à `ECTOPLASM_MINE` et `LURE` ; l'interface `Gadget` reste reportée. `Inventory(GameConfig)` initialise les stocks depuis `STARTING_MINE_COUNT` / `STARTING_LURE_COUNT`, expose `getCount`, `add(type, quantity > 0)` et `consume(type)`. La consommation à zéro renvoie `false` sans modifier le stock. L'inventaire n'orchestre ni les mines ni les appâts : l'appelant consomme après un placement ou une activation réussie. Aucune capacité maximale ; le faisceau, les lootboxes et le Fantômedex restent hors périmètre.

> Décision : `BeamProfile(GameConfig)` démarre au niveau 1 et expose `getLevel`, `getRequiredSeconds`, `getAngleToleranceDeg` et `upgrade()`. Le temps requis vaut `max(BEAM_MIN_REQUIRED_SECONDS, BEAM_BASE_REQUIRED_SECONDS - BEAM_TIME_REDUCTION_PER_LEVEL_S * (niveau - 1))`. La tolérance vaut `BEAM_BASE_ANGLE_TOLERANCE_DEG + BEAM_ANGLE_TOLERANCE_STEP_DEG * ((niveau - 1) / BEAM_LEVELS_PER_TOLERANCE_STEP)` en division entière ; les constantes `BEAM_ANGLE_TOLERANCE_STEP_DEG = 2,0` et `BEAM_LEVELS_PER_TOLERANCE_STEP = 2` ont été validées et ajoutées à `SPEC.md` §6.4. `upgrade()` renvoie `false` au niveau maximum sans modifier l'état. `CaptureController` continue d'utiliser ses constantes de base ; le branchement effectif du profil sera fait à une étape dédiée.

> Décision : `FloorProvider` est une interface autonome avec une méthode par défaut `int getFloor(GameInput input)` qui valide l'entrée puis retourne `0` en V1. Aucune implémentation séparée n'est créée ; une classe anonyme suffit pour les tests et la V2 surchargera la méthode. `GameSession` et `GameInput` ne sont pas modifiés : `GameInput.floor` reste à 0 en V1.

> Décision : `MapProjection(PlayArea)` centre la projection sur le centre de la boîte englobante du polygone extérieur et reprend le rayon terrestre de la zone jouable. `project(LatLon)` renvoie un `MapPoint` ENU en mètres (`x` est, `y` nord) et `unproject(xM, yM)` fournit l'inverse pour la cartographie. La formule est équirectangulaire locale : `x = R * Δlon * cos(latOrigine)`, `y = R * Δlat`. La couche UI inversera `y` pour `Canvas`. Les coordonnées non finies sont rejetées à la déprojection.

> Contrainte : ne pas créer `WorldMap`, `FloorMap` ni `Floor` en J1. `FloorProvider` est la seule abstraction d'étage.

### Vérifications J1

- [x] Aucun import `android.*` dans `domain`.
- [x] `./gradlew test` vert.
- [x] Mettre à jour `PLAN.md` et `JOURNAL.md`.

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

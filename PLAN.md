# PLAN - Ghost Radar V1

## Reprise rapide

- **Dernière étape validée** : J2 terminé + début J3 — CameraX/Play Services, capteurs Android, collecteur 100 ms, `PreviewView`, filtre bleu/IR, glitchs 500 ms, HUD `● REC`/horloge `HH:mm:ss`, jauge 10 segments, portrait verrouillé, pause arrière-plan ; boucle pure `GameLoop`, pause lifecycle et glitchs de transition ; audit pré-J3 et arbitrages Q1–Q8 enregistrés ; **B1/B2 corrigés en TDD** : helper partagé `Validation` (finitude, plages, entiers, objets, delta), `GameConfig.Builder.build()` valide tous les champs avec messages nommant le champ, `LatLon` rejette NaN/infini/hors plage, et la même politique est appliquée à `Ghost`, `GhostMovement`, `GhostSpawner`, `GeoPolygon`, `PlayArea`, `HauntedZone` ; tests de rejet ajoutés et trois tests existants adaptés à la validation centralisée ; **C1 corrigé en TDD** : `Validation.requireSameRadius` vérifie l'égalité stricte des rayons (`PlayArea` vs polygones, `GameConfig` vs `PlayArea` dans `GhostMovement`/`EctoplasmMine`/`GhostSpawner`, `HauntedZone.getEarthRadiusM()` dans `trySpawn`), sans changement de signature ; 375 tests verts (44 classes) ; **lot gameplay A2/A3/A5/A6/Q6 corrigé en TDD** : mines ignorées sur fantôme stunné, flags réservés documentés, `CaptureRules` pures, cooldown ancré sur l'heure réelle et `spawnAdmin` contraint à la play area ; 382 tests verts (45 classes) ; **Q4/A4 corrigé en TDD** : `CaptureController(GameConfig, BeamProfile)` (surcharge `(GameConfig)` conservée), temps/tolérance figés dans `start()`, alias `CAPTURE_*` dérivés de `BEAM_BASE_*` avec validation d'égalité, test du niveau 5 (3 s / 16°) et du figage ; 387 tests verts (45 classes) ; **B3 corrigé en TDD** : `GeoPolygon` rejette les polygones colinéaires (aire nulle) et uniformise `contains`/`nearestVertex`/`closestPointOnBoundary` en `IllegalArgumentException` pour un point `null` ; 390 tests verts (45 classes) ; **B4 corrigé en TDD** : `PlayArea` valide la topologie des obstacles (strictement intérieurs, non égaux à l'extérieur, intérieurs disjoints, contact sommet/bord entier autorisé), `SPEC.md` documenté, un test `HauntedZoneTest` adapté à un obstacle valide ; 396 tests verts (45 classes) ; **B5 corrigé en TDD** : `MapProjection` refuse origine `|lat| >= 80°` et étendue en longitude > 180°, limites documentées dans `MapProjection`/`SPEC.md` ; 399 tests verts (45 classes) ; **B7 corrigé en TDD** : `Inventory.add` utilise `Math.addExact` et lève `IllegalArgumentException` sans modifier le stock en cas de dépassement ; 400 tests verts (45 classes) ; **B8 corrigé en TDD** : `GameInput` applique l'invariant V1 (`floor == 0`, `pressureHpa` nul ou fini strictement positif) au constructeur et aux setters, `SPEC.md`/`GameSnapshot` documentés, `FloorProvider` inchangé ; 405 tests verts (45 classes) ; **C2 corrigé** : budgets de retry documentés comme paramètres d'algorithme (hors `GameConfig`) avec comportement d'épuisement, tests déterministes à `maxAttempts = 1` et `maxCenterAttempts = 1` ; 407 tests verts (45 classes) ; **B6 terminé** (centre = moyenne des sommets documentée, test concave), **D4 terminé** (formulation « composants terminés ; intégrations différées », test mine renommé sur le booléen) ; **rapport A1–D4 intégralement traité (20/20 cases cochées)** ; 408 tests verts (45 classes), APK debug compilé, aucun import `android.*` dans `domain`.
- **État J1** : **composants terminés ; intégrations différées** — le domaine pur V1 est implémenté et testé ; `BeamProfile` est branché dans `CaptureController` (Q4), mais les vecteurs de force de capture restent à intégrer (voir dettes ci-dessous). `GameEvent`, `GameEventType`, `GameListener` et `Gadget` restent explicitement reportés hors J1, et les drapeaux de règles sont réservés V1 sans variante.
- **Prochaine action immédiate** : aucune correction du rapport en attente ; sur nouvelle directive, traiter les intégrations différées (vecteurs de force de capture, événements `GameEvent`, drapeaux de règles) ou brancher le flux spawn/capture du J3 sur une `PlayArea`/`HauntedZone` issue de la cartographie.

> Détail : `SPEC.md` intégré, `PLAN.md` créé, `AGENTS.md` actualisé, commande `/jalon1` créée. Les **composants du Jalon 1 sont terminés ; les intégrations sont différées** : `LatLon`, `GeoMath`, `GeoPolygon`, `PlayArea`, `HauntedZone`, `RandomSource`, `GameState`, `GameConfig`, `GameInput`, `GameSnapshot`, `GameClock`, `GameSession`, `GhostSpawner`, `Ghost`, `GhostState`, `GhostMovement`, `NoiseMonitor`, `CaptureController`, `EctoplasmMine`, `Lure`, `GadgetType`, `Inventory`, `BeamProfile`, `FloorProvider` et `MapProjection` sont implémentés et testés. Restent reportés : les vecteurs de force de capture, `GameEvent`/`GameEventType`/`GameListener`/`Gadget`, et les drapeaux de règles inertes (réservés V1, sans variante). `BeamProfile` est désormais branché via `CaptureController(GameConfig, BeamProfile)` (Q4).

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
- [x] `EctoplasmMineTest` : rayon 3 m, stun 10 s, one-shot, max 3, visibilité carte, `update` renvoie `true` une seule fois (booléen signalant l'alerte, aucun `GameEvent`) et ne remet pas le stun à zéro ; cas à deux mines ajouté en D1.
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

> Décision : `CaptureController.start(playerPosition, ghost)` exige une distance strictement inférieure à 4 m, puis le contrôleur consomme `(aimErrorDeg, beamActive, deltaSeconds)`. Le verrou est `beamActive && |erreur| <= 12°`, sans hystérésis. Le timeout de 30 s couvre tout le combat, verrou perdu compris ; le succès est évalué avant le timeout. Le fantôme est réancré à chaque tick et le stun ne bloque pas la capture. `BeamProfile` est intégré depuis Q4 (constructeur à deux arguments) ; les vecteurs de force restent à intégrer dans une étape dédiée.

> Décision : `EctoplasmMine(GameConfig, PlayArea)` gère le champ de mines et expose des `PlacedMine` imbriquées. Le placement exige une position jouable, moins de `MINE_MAX_ACTIVE` mines non déclenchées et au moins `MINE_MIN_DISTANCE_M` de chacune. Au plus une mine se déclenche par tick (la plus proche à moins de `MINE_TRIGGER_RADIUS_M`) ; `update` renvoie `true` pour signaler l'alerte. Une mine déclenchée reste visible tant que le fantôme est stunné. Les flags `MINE_ONE_SHOT` et `MINE_PERSIST_UNTIL_TRIGGERED` ne créent pas de variante non spécifiée : le comportement reste one-shot et persistant jusqu'au déclenchement.

> Décision (audit pré-J3) : la visibilité est affinée — toutes les mines non déclenchées restent visibles, et pendant un stun seule la mine qui l'a provoqué est ajoutée à la carte. `EctoplasmMine.getActiveStun(Ghost)` expose un `ActiveStun` immuable (mine, `isTriggered()`, temps restant) pour permettre à l'UI d'afficher halo/alerte ; l'état est nettoyé dès que le fantôme n'est plus stunné, donc une mine ancienne ne réapparaît pas lors d'un stun ultérieur.

> Décision : `Lure` ne porte que l'activation, la durée et le one-shot ; `GhostMovement` conserve l'attraction, la priorité et le clamp. L'activation est refusée sans consommation sur un fantôme stunné. Une fois actif, le timer continue pendant un stun ultérieur ; l'objet reste consommé après expiration.

> Décision : `GadgetType` limite les consommables à `ECTOPLASM_MINE` et `LURE` ; l'interface `Gadget` reste reportée. `Inventory(GameConfig)` initialise les stocks depuis `STARTING_MINE_COUNT` / `STARTING_LURE_COUNT`, expose `getCount`, `add(type, quantity > 0)` et `consume(type)`. La consommation à zéro renvoie `false` sans modifier le stock. L'inventaire n'orchestre ni les mines ni les appâts : l'appelant consomme après un placement ou une activation réussie. Aucune capacité maximale ; le faisceau, les lootboxes et le Fantômedex restent hors périmètre.

> Décision : `BeamProfile(GameConfig)` démarre au niveau 1 et expose `getLevel`, `getRequiredSeconds`, `getAngleToleranceDeg` et `upgrade()`. Le temps requis vaut `max(BEAM_MIN_REQUIRED_SECONDS, BEAM_BASE_REQUIRED_SECONDS - BEAM_TIME_REDUCTION_PER_LEVEL_S * (niveau - 1))`. La tolérance vaut `BEAM_BASE_ANGLE_TOLERANCE_DEG + BEAM_ANGLE_TOLERANCE_STEP_DEG * ((niveau - 1) / BEAM_LEVELS_PER_TOLERANCE_STEP)` en division entière ; les constantes `BEAM_ANGLE_TOLERANCE_STEP_DEG = 2,0` et `BEAM_LEVELS_PER_TOLERANCE_STEP = 2` ont été validées et ajoutées à `SPEC.md` §6.4. `upgrade()` renvoie `false` au niveau maximum sans modifier l'état. `CaptureController(GameConfig, BeamProfile)` applique désormais effectivement temps requis et tolérance du profil, figés dans `start()` ; les alias `CAPTURE_*` restent dérivés des constantes de base (Q4).

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

- [x] Ajouter CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) — dépendance pré-approuvée.
- [x] Ajouter Play Services Location — dépendance pré-approuvée.
- [x] Créer les interfaces capteurs dans `com.example.ghostradar.sensors`.
- [x] Écrire les tests des contrats et des filtres purs (phase rouge puis verte).
- [x] Créer les modèles `GpsSample`, `CompassSample`, `NoiseSample`, `AccelerometerSample`, `TiltSample` et `CompassAccuracy`.
- [x] Créer `GpsFixFilter`, `CompassHeadingFilter`, `RmsMeter`, `TiltCalculator` et `SensorFrame`.
- [x] Implémenter `AndroidGpsSensor` avec `FusedLocationProviderClient` (accuracy ≤ 20 m, 1 Hz, 1 m).
- [x] Implémenter `AndroidCompassSensor` (`TYPE_ROTATION_VECTOR`, fallback magnétomètre, low-pass 0,15).
- [x] Implémenter `AndroidMicrophoneSensor` (`AudioRecord` 44,1 kHz mono PCM16, blocs 20 ms, RMS → dBFS).
- [x] Implémenter `AndroidAccelerometerSensor` (`TYPE_ACCELEROMETER`, 50 Hz).
- [x] Gérer les permissions runtime (localisation, micro).
- [x] Créer `SensorFrame` immuable (GPS, boussole, bruit, accéléromètre, tilt) et le tester.
- [x] Brancher `SensorFrame` au tick fixe de 100 ms via `AndroidSensorFrameCollector` (`GameInput` reste floor/pressure).

> Décision : `SensorFrame` est séparé de `GameInput`, qui reste limité à `floor` et `pressureHpa`. Les filtres purs vivent dans `domain.sensors`, les contrats dans `sensors`, et les implémentations Android seront placées dans un sous-paquet séparé.

### Caméra & HUD

- [x] Intégrer `CameraX PreviewView` en plein écran.
- [x] Ajouter le filtre bleu/IR (alpha 0,15).
- [x] Ajouter les hooks de glitch (500 ms) à l'apparition et à la capture.
- [x] HUD `● REC` clignotant + horloge `HH:MM:SS`.
- [x] Jauge de bruit discrète (-60 à 0 dBFS).
- [x] Verrouiller le portrait et gérer la pause en arrière-plan.

> Décision : le filtre bleu/IR utilise `BLUE_IR_RGB = 0x003366`, un overlay `View` XML plein écran et la classe pure `CameraFilterStyle` (ARGB calculé depuis `GameConfig.getCameraBlueFilterAlpha()`), écrite et testée en JUnit avant le branchement UI.

> Décision : les glitches utilisent des hooks UI explicites (`triggerSpawn`/`triggerCapture`, sans modifier `GameSession` ni introduire `GameEvent`), 6 bandes horizontales `#66CCFF` (hauteur 4–12 px, opacité 0,35, refresh 50 ms) pilotées par la classe pure `CameraGlitchController` testée en JUnit avant le branchement `GlitchOverlayView`.

> Décision : le HUD est en haut-gauche (marge et texte 16), fond noir `0x99000000`, `● REC` rouge `#FF3333` clignotant 500 ms on/off et horloge blanche `#FFFFFF` affichant l’heure locale du téléphone (`System.currentTimeMillis()` + `ZoneId.systemDefault()`) rafraîchie toutes les secondes ; `HudStyle`, `HudClockFormatter` et `RecBlinkController` sont des classes pures JUnit écrites avant le branchement `MainActivity`.

> Décision : la jauge de bruit est verticale à droite (marge 16 dp), centrée verticalement, 10 segments 6×10 dp espacés de 2 dp remplis de bas en haut ; le niveau vient du `NoiseMonitor` déjà intégré au `SensorFrameCollector` (`getNoiseGaugeLevel()` / `getSmoothedNoiseDbfs()`), sans doublon de monitor ; couleurs vides `0x66FFFFFF`, jaune `0xFFFFD54F` sous -40 dBFS, orange `0xFFFF9800` de -40 au seuil `-30`, rouge `0xFFFF3333` au-dessus ; icône micro vectorielle blanche 16×16 dp à 8 dp sous la jauge ; `NoiseGaugeStyle` et `NoiseGaugeFormatter` sont des classes pures JUnit écrites avant le branchement `NoiseGaugeView`.

> Décision : le portrait est verrouillé déclarativement par `android:screenOrientation="portrait"` sur `MainActivity` ; la pause reste sur `onPause` et arrête déjà capteurs, caméra, HUD et glitch. La simulation n’étant pas encore branchée, sa pause sera raccordée lors de la boucle de jeu visuelle, sans ajouter de porte lifecycle artificielle maintenant.

### Vérifications J2

- [x] Tests unitaires des adaptateurs purs (sans Android) si possible.
- [x] `./gradlew test` vert.
- [x] `./gradlew assembleDebug` OK.
- [x] Mettre à jour `PLAN.md` et `JOURNAL.md`.

---

## Jalon 3 — Radar, carte & boucle de jeu visuelle

### Boucle de jeu

- [x] Boucle pure `GameLoop` : frame capteur → `GameSession.update()`, `GameSnapshot` et listener de transitions.
- [x] Pause/reprise lifecycle de la simulation (`pauseForLifecycle`/`resumeFromLifecycle`).
- [x] Glitchs spawn/capture déclenchés depuis les transitions de la boucle.
- [ ] Brancher un vrai flux spawn/capture (play area, zone hantée, fantôme, distance de capture).

> Décision : `GameLoop` vit dans `domain/game`, reste en Java pur et ne détient aucune géométrie ; il consomme `SensorFrame`, avance la `GameSession`, expose `GameSnapshot` et notifie `onStateChanged(previous, current)` au prochain tick, y compris pour une transition déclenchée depuis l'extérieur. `MainActivity` mappe `EXPLORATION→TRACKING` sur `triggerSpawnGlitch()` et toute entrée dans `CAPTURE` sur `triggerCaptureGlitch()`. La pause lifecycle est une API distincte de la pause hors zone, et les deux peuvent se chevaucher sans décaler deux fois les timers.

### Audit pré-J3 (terminé)

- [x] Relecture intégrale de `SPEC.md`, `PLAN.md`, des sources Java et des tests.
- [x] Audit de la qualité des tests : aucun remplissage artificiel (3 `assertNotNull` seulement), suites denses et assertions sur des valeurs comportementales ; seul `ExampleUnitTest` (`2 + 2`) était un test modèle sans valeur, supprimé.
- [x] Conformité vérifiée : `GameConfig`, `CaptureController`, `NoiseMonitor`, `GpsFixFilter`, `CompassHeadingFilter`, `TiltCalculator`, `BeamProfile`, géométrie, capteurs Android, UI pure (glitch, HUD, jauge).
- [x] Bug confirmé corrigé en TDD : `GhostMovement` clampait tous les déplacements sauf l'arrivée de l'appât, qui plaçait le fantôme sur un joueur hors zone (`SPEC.md` §4).
- [x] Visibilité des mines arbitrée : toutes les mines actives restent visibles ; pendant le stun, seule la mine qui l'a provoqué est ajoutée à la carte et exposée via `getActiveStun(ghost)` (`ActiveStun` : mine, état `isTriggered()`, temps restant). Les mines déclenchées lors d'un stun précédent ne réapparaissent pas.
- [x] Flags sans effet documentés sans variante : `MINE_ONE_SHOT`, `MINE_PERSIST_UNTIL_TRIGGERED`, `LURE_ONE_SHOT`, `LURE_PRIORITY_OVER_FLEE`, `LURE_AFFECTS_STUNNED`.
- [x] Relance des capteurs sur accord runtime protégée par le garde `cameraResumed` (pas de redémarrage en arrière-plan).
- [x] `./gradlew test` → 356 tests, 0 échec ; `./gradlew assembleDebug` → OK.

### Correction du rapport d'incohérences J1 — décisions Q1–Q8 et suivi

> Décisions utilisateur enregistrées le 2026-11-23 (aucune ambiguïté restante avant code) :
> - **Q1 (A2)** : une mine ignorée si le fantôme est déjà stunné — `update()` retourne `false`, timer non remis à zéro.
> - **Q2 (A3)** : tous les flags inertes documentés « réservés V1, sans variante de comportement » — y compris `MINE_ALERT_ON_STUN` et les deux flags `ADMIN_*`. Aucune branche logique inventée.
> - **Q3 (A5)** : cooldown 30 s démarrant à la **fin du TRACKING** (comportement actuel) ; documenter qu'il peut expirer pendant la capture et ajouter le test dédié.
> - **Q4 (A4)** : brancher `BeamProfile` dans `CaptureController` dès J3 ; dériver/supprimer les `CAPTURE_*` dupliqués ; tester le niveau 5 (3 s, 16°).
> - **Q5 (A1/D2)** : pas de test « joueur dans un obstacle » — contrat = joueur toujours valide ; le test hors polygone suffit (déjà ajouté).
> - **Q6 (B6)** : `spawnAdmin` n'ignore que les zones hantées et la probabilité ; reste contraint à la play area hors obstacles — documenter et tester.
> - **Q7 (B1/B2/B8)** : validation **stricte et centralisée** dans `GameConfig.Builder.build()` + helpers partagés ; `LatLon` rejette NaN/infini/hors plage dès la construction. Les surcharges de tests invalides sont adaptées, pas la validation assouplie.
> - **Q8 (périmètre)** : priorité décroissante de sévérité, `PLAN.md`/`JOURNAL.md` tenus à jour en continu et cases cochées au fur et à mesure.
> - **Arbitrages complémentaires (2026-11-23 16:32)** : C1 = **validation d'égalité** des rayons reçus (`IllegalArgumentException` si divergence, signatures inchangées) ; A6 = **règles pures dédiées** (type `CaptureRules`, sans modifier `Mine`/`Lure`) ; B8 = **validation dans `GameInput`** (`floor == 0`, `pressureHpa` fini/positif si présent) ; C2 = **documenter** `maxAttempts`/`maxCenterAttempts` comme paramètres algorithmiques (pas de nouvelle constante, test à 1 tentative) ; B3 = **`IllegalArgumentException` partout** pour les arguments `null` de `GeoPolygon`.
> - **Arbitrages gameplay (2026-11-23 17:08)** : Q6 = `spawnAdmin` refuse un point hors play area ou dans un obstacle avec **`IllegalArgumentException`** (signature `LatLon` inchangée, jamais de `null`) ; A5 = cooldown ancré sur le **`nowMs` réel de la transition** (pas sur l'échéance théorique du screamer), avec test de tick retardé + test d'expiration pendant la capture.
> - **Arbitrages Q4/A4 (2026-11-23 17:23)** : `CaptureController` reçoit `BeamProfile` via un **nouveau constructeur `(GameConfig, BeamProfile)`**, l'ancien `(GameConfig)` restant disponible et créant un profil niveau 1 ; temps requis et tolérance sont **figés dans `start()`** (l'upgrade n'intervient qu'entre deux combats, hors lootbox) ; `CAPTURE_REQUIRED_SECONDS` et `CAPTURE_ANGLE_TOLERANCE_DEG` deviennent des **alias dérivés** de `BEAM_BASE_*` (getters conservés, surcharge explicite acceptée seulement si elle est égale à la base, sinon `IllegalArgumentException`).
> - **Arbitrage B4 (2026-11-23 19:21)** : la topologie est **validée dans `PlayArea`** (l'UI de cartographie n'existe pas encore) ; chaque obstacle est **strictement intérieur** au polygone extérieur (aucun sommet ni bord sur la frontière, obstacle égal à l'extérieur refusé) ; les obstacles **ne doivent pas avoir d'intérieurs qui se chevauchent**, mais peuvent **partager un sommet ou un bord entier** (trous adjacents).
> - **Arbitrage B5 (2026-11-23 20:22)** : `MapProjection` refuse une origine de **|latitude| >= 80°** et un polygone extérieur dont l'**étendue en longitude > 180°** (antiméridien), les deux avec `IllegalArgumentException` au constructeur ; la finitude de `LatLon` est documentée comme garantie héritée de B2 (pas de garde morte dans `project`).
> - **Arbitrage B7 (2026-11-23 20:26)** : `Inventory.add` utilise `Math.addExact` et convertit l'`ArithmeticException` en **`IllegalArgumentException`** ; le stock reste inchangé en cas de dépassement (pas de saturation).
> - **Arbitrage B6 (2026-11-23 20:35)** : `HauntedZone.getCenter()` **conserve la moyenne arithmétique des sommets** pour un polygone ; elle est documentée comme point de référence (pas un centroïde d'aire), sans garantie d'appartenance pour un polygone concave. L'UI doit utiliser `contains()`.

Bilan du rapport au moment des décisions : **1 réglé, 5 partiels, 14 non traités**. Liste ordonnée (cases à cocher au fur et à mesure) :

- [x] **A1 (Élevée)** — clamp de l'arrivée exacte de l'appât (`GhostMovement` + test hors polygone). Q5 : pas de test obstacle.
- [x] **B1 (Élevée)** — validation centralisée de `GameConfig.Builder.build()` : finitude, plages min<=max, invariants (`captureRequired<=timeout`, `beamMin<=beamBase`, poids >= 0, tick > 0, stocks >= 0, pity >= 0), enums/sets non null, messages nommant le champ. `tiltGain = 0` reste accepté (valeur limite légitime, test `TiltCalculatorTest.zeroGainProducesZeroOutput` conservé).
- [x] **B2 (Élevée)** — helpers partagés `Validation` (`requireFinite`, `requirePositiveFinite`, `requireNonNegativeFinite`, `requireRange`, `requirePositiveInt`, `requireNonNegativeInt`, `requireNonNull`, `requireDeltaSeconds`, `requireLatitude`, `requireLongitude`) ; `LatLon` rejette NaN/infini/hors plage ; même politique appliquée à `Ghost`, `GhostMovement`, `GhostSpawner`, `GeoPolygon`, `PlayArea`, `HauntedZone`. `MapProjection` hérite de la validation `LatLon` ; `GameInput` est traité avec B8.
- [x] **C1 (Moyenne)** — validation d'égalité stricte des rayons entre `GameConfig`/`PlayArea`/`GeoPolygon`/`HauntedZone` via `Validation.requireSameRadius` (aucune rupture d'API) ; tests avec deux rayons divergents (`6_400_000.0` vs `6_371_000.0`) dans `PlayAreaTest`, `GhostMovementTest`, `EctoplasmMineTest`, `GhostSpawnerTest`, plus `ValidationTest` ; `MapProjectionTest` adapté au contrat.
- [x] **A2 (Moyenne)** — `EctoplasmMine.update()` retourne `false` immédiatement si le fantôme est déjà stunné (timer non remis à zéro, aucune mine consommée) ; test avec deux mines à 5,1 m et fantôme dans la zone commune.
- [x] **A3 (Moyenne)** — tous les drapeaux inertes documentés « réservés V1, sans variante » dans `GameConfig` et `SPEC.md` §6, y compris `MINE_ALERT_ON_STUN` et les deux `ADMIN_*` (Q2).
- [x] **B3 (Moyenne)** — `GeoPolygon` : `minVertexDistanceM`/`earthRadiusM` finis > 0 et sommets finis déjà validés en B2 ; ajout du rejet des polygones colinéaires (aire nulle) et de `IllegalArgumentException` uniforme pour les points `null` des trois méthodes de requête ; tests dédiés(+ garde-fou polygone fin mais non dégénéré).
- [x] **B4 (Moyenne)** — validation dans `PlayArea` : obstacles strictement intérieurs (sommets + bords, égalité refusée), intérieurs disjoints (intersection franche, sommet strictement dans l'autre, bords colinéaires partiellement superposés), partage d'un sommet ou d'un bord entier autorisé ; 6 tests dédiés, `SPEC.md` documenté, `HauntedZoneTest` adapté.
- [x] **D1 (Moyenne)** — test mine trompeur remplacé par `secondMineInCommonZoneDoesNotReStunAlreadyStunnedGhost` (deux mines à 5,1 m, fantôme dans la zone commune, retour `false` et timer préservé).
- [x] **D2 (Moyenne)** — branche d'arrivée exacte de l'appât couverte par `lureArrivalClampsPlayerOutsidePlayArea` (Q5 : pas d'obstacle) — re-vérifié.
- [x] **A4/Q4 (Moyenne)** — `CaptureController(GameConfig, BeamProfile)` (surcharge `(GameConfig)` = niveau 1), temps/tolérance figés dans `start()`, `CAPTURE_*` conservés comme alias dérivés de `BEAM_BASE_*` (surcharge explicite égale seulement acceptée) ; tests niveau 5 (3 s, 16°), figage après upgrade et rejet des alias divergents.
- [x] **A5/Q3 (Faible)** — cooldown ancré sur le `nowMs` réel de la transition, documenté dans `SPEC.md` §4 et `GameSession` (peut expirer pendant la capture) ; tests `cooldownCanExpireDuringCaptureAndAllowImmediateNewTracking` et `cooldownUsesActualTransitionTimeWhenTickIsLate`. La fin du screamer reste ancrée sur son échéance théorique (hors périmètre de Q3).
- [x] **A6 (Observation)** — classe pure `CaptureRules` (`isNoiseAlertEffective`, `canUseGadget`) + `CaptureRulesTest` (3 tests) portant la règle « bruit/gadgets sans effet pendant CAPTURE » ; les objets `EctoplasmMine`/`Lure` et `GameLoop` restent inchangés.
- [x] **B5 (Faible)** — `MapProjection` refuse au constructeur `|latitude d'origine| >= 80°` (garde sur `cos(origin)`) et une étendue en longitude > 180° (antiméridien) ; limites documentées dans la Javadoc et `SPEC.md` §5 ; finitude garantie par `LatLon` (B2) avec test de non-régression — 3 tests ajoutés.
- [x] **B6 (Faible)** — `GhostSpawner`/`HauntedZone` : plages min/max et rayons NaN déjà validés (B1/B2) ; `spawnAdmin` refuse hors play area/obstacle (Q6) ; comportement d'échec des budgets documenté en C2 ; `getCenter()` conserve la moyenne des sommets, documentée comme non garantie dans un polygone concave, avec test de caractérisation dédié.
- [x] **B7 (Faible)** — `Inventory.add` utilise `Math.addExact`, convertit l'`ArithmeticException` en `IllegalArgumentException` et laisse le stock inchangé ; test de dépassement (stock plein + quantité max).
- [x] **B8 (Faible)** — `GameInput` valide `floor == 0` et `pressureHpa` nul ou fini strictement positif dans le constructeur et les setters (valeur inchangée en cas de refus) ; `GameSnapshot` hérite de valeurs valides, `FloorProvider` inchangé, `SPEC.md` §2 documenté ; 4 tests ajoutés et 3 fixtures adaptées (`GameInputTest`, `FloorProviderTest`, `GameLoopTest`).
- [x] **C2 (Faible)** — `maxAttempts` (`GhostSpawner.trySpawn`) et `maxCenterAttempts` (`HauntedZone.randomPolygon`) documentés comme **paramètres d'algorithme non configurables** (Javadoc + `SPEC.md` §6) avec épuisement → `Optional.empty()` / `IllegalStateException` ; 2 tests déterministes dont la valeur `1` (échec puis succès avec 2 tentatives).
- [x] **D3 (Faible)** — `GameConfigTest` : tests de rejet NaN/infini/négatif/min>max/null (traités avec B1/B2).
- [x] **D4 (Faible)** — `PLAN.md` : « composants J1 terminés ; intégrations différées » visible en reprise rapide avec la liste des reports à jour (BeamProfile déjà branché en Q4) ; description d'`EctoplasmMineTest` alignée sur le booléen renvoyé par `update` (test renommé `updateReturnsTrueOnceAndDoesNotResetStun`).

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

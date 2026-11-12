# Cahier des Charges - Ghost Radar

## 1. Concept & Ambiance

Application mobile parodique de chasse aux fantômes sur le campus (N7). L'écran principal affiche le flux vidéo de la caméra arrière en temps réel avec un habillage façon caméscope d'enquête paranormale / Ghostbusters, conçu pour être filmé en enregistrement d'écran.

Décisions générales :
- Interface intégralement en français.
- Orientation portrait verrouillée.
- Mise en pause automatique lorsque l'application passe en arrière-plan (capteurs + simulation).
- Toutes les valeurs numériques de ce document sont des valeurs par défaut configurables dans `GameConfig` (ou équivalent). Aucun nombre magique ne doit être dispersé dans le code.
- La simulation utilise un pas de temps fixe de 100 ms (`TICK_MS`), avec `GameClock` et `RandomSource` injectables (seed) pour des tests JUnit déterministes.

## 2. Capteurs & Rôles

### Périmètre V1 (Extérieur - Cour / Campus) — 5 capteurs

1. **Caméra arrière (`CameraX PreviewView`)**
   - Arrière-plan plein écran permanent.
   - Superposition d'un filtre visuel bleu/infrarouge léger : alpha `0,15` (`CAMERA_BLUE_FILTER_ALPHA`).
   - Effets de parasites/glitchs lors de l'apparition ou de la capture d'un fantôme : durée `500 ms` (`CAMERA_GLITCH_DURATION_MS`), déclencheurs configurables.
   - Les valeurs visuelles sont des constantes modifiables pour affiner le rendu en J2.

2. **GPS (`FusedLocationProviderClient`)**
   - Cartographie du périmètre jouable et des zones hantées.
   - Suivi temps réel du joueur et déplacement du fantôme contraint à l'intérieur du polygone.
   - Distance calculée par la formule de Haversine avec `R = 6 371 000 m` (`EARTH_RADIUS_M`), en distance linéaire 2D (pas de distance de marche).
   - Seuils de jeu configurables : `MINE_TRIGGER_RADIUS_M = 3,0 m`, `CAPTURE_DISTANCE_M = 4,0 m`.
   - Filtrage GPS : `GPS_ACCURACY_MAX_M = 20 m`, `GPS_UPDATE_INTERVAL_MS = 1 000 ms`, `GPS_MIN_DISTANCE_M = 1 m`.
   - Position GPS brute en V1 (`GPS_SMOOTHING_ENABLED = false`). L'architecture doit permettre d'ajouter un lissage (moyenne glissante/Kalman) en V2+ sans changer les règles métier.
   - Structure prête pour le filtrage par étage en V2 : `GameInput` contient `int floor` (0 par défaut) et `Double pressureHpa` (nullable), et `FloorProvider` retourne 0 en V1.

3. **Boussole (`Sensor.TYPE_ROTATION_VECTOR` ou Magnétomètre)**
   - Orientation en direct du capteur fantasmagorique (radar) selon l'azimut du téléphone.
   - `TYPE_ROTATION_VECTOR` prioritaire ; fallback magnétomètre si indisponible (`COMPASS_FALLBACK_ENABLED = true`).
   - Lissage angulaire passe-bas : `COMPASS_LOW_PASS_ALPHA = 0,15`.
   - Azimut : `0° = Nord`, sens horaire, plage `0–360°`.
   - Les mesures à précision insuffisante sont ignorées (`COMPASS_IGNORE_UNRELIABLE = true`).

4. **Microphone (`AudioRecord`)**
   - Mesure continue du niveau sonore ambiant (RMS).
   - Conversion RMS → dBFS, fenêtre de calcul `200 ms` (`NOISE_WINDOW_MS`), lissage exponentiel (`NOISE_SMOOTHING_ALPHA = 0,2`).
   - Seuil de bruit : `-30 dBFS` (`NOISE_THRESHOLD_DBFS`), hystérésis `2 s` (`NOISE_HYSTERESIS_S`).
   - Furtivité : si le bruit dépasse le seuil, le fantôme repère le joueur, reste alerté `3 s` après retour sous le seuil (`NOISE_ALERT_HOLD_S`), et sa vitesse de fuite double.
   - Jauge de bruit HUD : plage `-60 dBFS` à `0 dBFS` (`NOISE_GAUGE_MIN_DBFS`, `NOISE_GAUGE_MAX_DBFS`).

5. **Accéléromètre (`Sensor.TYPE_ACCELEROMETER`)**
   - Contrôle du viseur pendant la phase de capture : le fantôme applique des vecteurs de force aléatoires sur le viseur et le joueur incline le téléphone en sens inverse pour maintenir le faisceau verrouillé.
   - Axes `roll`/`pitch`, téléphone en portrait.
   - Deadzone `5°` (`TILT_DEADZONE_DEG`), gain `1:1` (`TILT_GAIN = 1,0`), clamp `±30°` (`TILT_CLAMP_DEG`), échantillonnage `50 Hz` (`ACCEL_SAMPLING_HZ`).
   - Pendant la capture, la position GPS du fantôme reste ancrée sur place : le combat est purement orientation/tilt, le joueur n'a pas à courir.

### Périmètre V2 (Extension Intérieur & Étages) — 6ᵉ capteur

6. **Baromètre (`Sensor.TYPE_PRESSURE`)**
   - Calibrage d'une pression de référence `P0` au rez-de-chaussée.
   - Détection automatique de l'étage actuel du joueur : `0,36 hPa` par étage (`PRESSURE_PER_FLOOR_HPA`), hystérésis `±0,1 hPa` (`FLOOR_HYSTERESIS_HPA`), calibration `5 s` (`BAROMETER_CALIBRATION_S`).
   - Numérotation des étages : `0 = RDC`, puis `1, 2, 3…` ; pas de sous-sol en V2.
   - En V1, l'altitude est ignorée : `GameInput.floor = 0` et `FloorProvider` retourne 0.
   - Les valeurs seront recalibrées lorsque la V2 sera développée et que les mesures réelles seront disponibles ; elles restent des constantes configurables.

## 3. Interface Utilisateur (Écran Principal & Navigation)

- **Fond** : flux caméra arrière plein écran + calque filtre bleu/infrarouge.
- **HUD Caméscope minimaliste (en haut)** :
  - Indicateur `● REC` (point rouge clignotant) + horloge temps réel (`HH:MM:SS`) + jauge discrète de bruit ambiant (micro).
- **Boutons d'action principaux (en bas / côtés)** :
  1. **Bouton `Capteur Fantasmagorique`** : affiche ou masque en overlay (façon capteur cardiaque Warzone) le radar orienté par la boussole indiquant la distance et la direction du fantôme.
     - Portée max `100 m` (`RADAR_RANGE_M`), orientation `phone-up`, distance chiffrée affichée, visibilité 360°.
     - Le radar n'est visible qu'en état `TRACKING` (`RADAR_VISIBLE_STATES = TRACKING`).
  2. **Bouton `Map`** : ouvre la vue carte 2D montrant le polygone extérieur, les polygones d'obstacles, la position du joueur, les zones hantées et les mines posées. Permet aussi d'accéder au **Mode Cartographie** (acquisition GPS automatique en marchant + édition manuelle des sommets du polygone et des obstacles).
  3. **Bouton `Inventaire`** : accès aux gadgets consommables, aux améliorations du faisceau, aux Lootboxes à ouvrir et au **Fantômedex**.
- **Mode Admin / Debug (bouton discret dans un coin, visible uniquement en debug)** :
  - Poser une "Zone Hantée" à la position GPS actuelle : cercle de rayon configurable depuis l'interface admin (`ADMIN_ZONE_DEFAULT_RADIUS_M = 25 m`).
  - Forcer l'apparition immédiate d'un fantôme à un endroit renseigné sur la carte, en ignorant les zones et la probabilité de déclenchement (`ADMIN_SPAWN_AT_MAP_POINT = true`, `ADMIN_IGNORE_ZONES_AND_PROBABILITY = true`).
- **Localisation** : tous les textes de l'interface sont en français.

## 4. Boucle de Jeu (Machine à états)

1. **`EXPLORATION`** : Le joueur se déplace dans la zone cartographiée. Lorsqu'il entre dans une Zone Hantée, une probabilité par seconde déclenche une alerte plein écran : `"ANOMALIE DÉTECTÉE"`.
   - Probabilité de base : `2 %/s` (`ZONE_TRIGGER_PROBABILITY_PER_S = 0,02`), constante ajustable.
   - Cooldown après la fin d'un tracking : `30 s` (`ZONE_TRIGGER_COOLDOWN_S`).
   - Les zones hantées sont générées sous forme de polygones aléatoires (`ZONE_POLYGON_MIN_VERTICES = 5`, `ZONE_POLYGON_MAX_VERTICES = 8`, `ZONE_RADIUS_MIN_M = 10`, `ZONE_RADIUS_MAX_M = 30`), avec un centre tiré aléatoirement dans le polygone extérieur, hors obstacles. En mode admin, la zone est un cercle de rayon configurable.
   - Le fantôme apparaît dans la zone hantée, à une distance du joueur comprise entre `15 m` (`GHOST_SPAWN_MIN_DISTANCE_M`) et `40 m` (`GHOST_SPAWN_MAX_DISTANCE_M`), valeurs initiales configurables à ajuster lors des tests GPS réels. Si aucun point valide de la zone ne satisfait cette contrainte, on choisit le point valide le plus proche de la cible.
   - Si le joueur sort du polygone jouable : avertissement + pause de la simulation (`PLAYER_OUT_OF_AREA_PAUSE = true`).

2. **`TRACKING`** : Le fantôme est actif et se déplace dans le polygone (observable sur le Capteur Fantasmagorique). Il fuit le joueur.
   - Vitesse de base : `1,2 m/s` (`GHOST_BASE_SPEED_MPS`), vitesse de fuite `2,4 m/s` (`GHOST_FLEE_MULTIPLIER = 2,0`) pendant l'alerte bruit.
   - Le fantôme est contraint à l'intérieur du polygone extérieur et à l'extérieur des polygones d'obstacles. Il est repoussé et glisse le long des bords (clamp + glissement), que ce soit pour la limite extérieure ou les obstacles.
   - **Mine à ectoplasme** :
     - Posée à la position GPS du joueur, obligatoirement dans le polygone jouable, à au moins `5 m` d'une autre mine (`MINE_MIN_DISTANCE_M`).
     - Immobilise le fantôme `10 s` (`MINE_STUN_DURATION_S`) s'il passe à moins de `3 m` (`MINE_TRIGGER_RADIUS_M`).
     - One-shot, maximum `3` mines actives (`MINE_MAX_ACTIVE`), persiste sur la carte jusqu'au déclenchement.
     - Visible sur la carte tant qu'elle n'est pas utilisée et pendant le stun du fantôme.
     - Déclenche une alerte/événement indiquant qu'un fantôme a été stun.
     - La capture reste possible pendant le stun : le joueur peut sortir le fantôme de la mine et le capturer directement.
   - **Appât** :
     - Attire le fantôme vers le joueur pendant `8 s` (`LURE_DURATION_S`), à la vitesse de `1,5 m/s` (`LURE_SPEED_MPS`).
     - Priorité sur la fuite et sur l'alerte bruit, one-shot, sans effet si le fantôme est stun.
     - Le type narratif de l'appât (sonore, ectoplasme, phéromones…) est purement cosmétique et pourra être choisi en J3 ; le domaine utilise le nom générique `Lure` et les règles ci-dessus.
     - Ne peut pas faire sortir le fantôme du polygone (clamp).

3. **`SCREAMER` -> `CAPTURE`** :
   - Quand `distance < 4 m` (`CAPTURE_DISTANCE_M`, constante), le point disparaît du radar.
   - Après un délai aléatoire de `1 à 2 s` (`SCREAMER_DELAY_MIN_S`, `SCREAMER_DELAY_MAX_S`), un **Screamer** (image plein écran + son + vibration) s'affiche pendant `2 s` (`SCREAMER_DURATION_S`), non skippable (`SCREAMER_SKIPPABLE = false`).
   - Pendant le Screamer, le GPS et le déplacement sont figés.
   - Le combat démarre ensuite par-dessus la caméra. Le joueur active son **Faisceau** et compense les mouvements du fantôme à l'**Accéléromètre** pour maintenir le viseur sur la cible pendant `5 s` cumulées (`CAPTURE_REQUIRED_SECONDS`).
   - Le fantôme applique des vecteurs de force aléatoires sur le viseur : changement toutes les `0,5 à 1,5 s` (`CAPTURE_FORCE_CHANGE_MIN_S`, `CAPTURE_FORCE_CHANGE_MAX_S`), amplitude `±20°` (`CAPTURE_FORCE_AMPLITUDE_DEG`).
   - Tolérance de verrouillage : `±12°` (`CAPTURE_ANGLE_TOLERANCE_DEG`).
   - Le temps cumulé se met en pause si le verrou est perdu, sans décroissance (`CAPTURE_PAUSE_ON_LOCK_LOST = true`, `CAPTURE_PROGRESS_DECAY_PER_S = 0`).
   - La position GPS du fantôme reste ancrée sur place pendant toute la capture (`CAPTURE_GHOST_POSITION_LOCKED = true`).
   - Échec : timeout de `30 s` (`CAPTURE_TIMEOUT_S`), le fantôme fuit, retour en `EXPLORATION`, aucune récompense.
   - Le bruit et les gadgets n'ont aucun effet pendant la capture (`NOISE_AFFECTS_CAPTURE = false`, `GADGETS_USABLE_DURING_CAPTURE = false`).
   - La capture est possible pendant le stun infligé par une mine.

4. **`RECOMPENSE` & `FANTOMEDEX`** :
   - Chaque capture rapporte exactement `1` **Caisse Paranormale** (`LOOTBOX_PER_CAPTURE = 1`), garantie même si l'identification échoue.
   - Tirage façon CS:GO : Mines, Appâts, ou amélioration de niveau du Faisceau.
     - Table initiale : `45 %` Mines (`LOOT_MINE_WEIGHT`), `45 %` Appâts (`LOOT_LURE_WEIGHT`), `10 %` Amélioration (`LOOT_BEAM_WEIGHT`).
     - Architecture modulaire pour les pourcentages et système de pity configurable (`PITY_ENABLED`, `PITY_MISSES_BEFORE_GUARANTEE`, `PITY_GUARANTEE_TYPE`).
   - Le fantôme rejoint le **Fantômedex** en tant qu'"Entité Non Identifiée" ; réussir un court **mini-jeu d'identification** (ex. calibrage de fréquence) révèle son identité parodique (nom de prof/cours, rareté, description).
     - Mini-jeu initial : curseur de fréquence `1–10 Hz` (`IDENTIFICATION_FREQ_MIN_HZ`, `IDENTIFICATION_FREQ_MAX_HZ`), tolérance `±0,5 Hz` (`IDENTIFICATION_TOLERANCE_HZ`), `3` essais (`IDENTIFICATION_ATTEMPTS`), échec = reste "non identifié" mais réessayable (`IDENTIFICATION_RETRY_ALLOWED = true`). Le mini-jeu est encapsulé dans un composant dédié et remplaçable, avec des constantes configurables.
     - Les identités (10 par défaut) seront fournies par l'utilisateur dans un fichier JSON ou format équivalent (`IDENTITY_COUNT = 10`).
     - L'identité révélée est purement cosmétique, sans bonus gameplay (`IDENTITY_HAS_GAMEPLAY_EFFECT = false`).

## 5. Carte, Polygones & Mode Cartographie

- **Périmètre jouable** : un polygone extérieur complet.
- **Obstacles** : zéro ou plusieurs polygones fermés intérieurs (bâtiments, zones interdites, patchs d'herbe, etc.) formant des trous dans la zone jouable. Le fantôme ne peut pas y entrer ; il est repoussé et glisse le long de leurs bords. Le joueur n'est pas physiquement bloqué en V1 (le GPS réel fait foi), mais les obstacles sont affichés sur la carte et signalés comme non praticables.
- **Mode cartographie** :
  1. L'utilisateur active l'enregistrement.
  2. Il marche pour capturer les positions GPS brutes : un point tous les `3 m` ou `2 s` (`CARTOGRAPHY_SAMPLE_DISTANCE_M`, `CARTOGRAPHY_SAMPLE_INTERVAL_MS`).
  3. À l'arrêt, l'application applique la simplification Douglas-Peucker avec une tolérance de `1 m` (`DOUGLAS_PEUCKER_TOLERANCE_M`) pour obtenir un polygone propre.
  4. Le polygone est éditable : déplacer, ajouter, supprimer des sommets, avec `undo` (`CARTOGRAPHY_UNDO_ENABLED = true`).
  5. En mode édition, l'utilisateur peut tracer manuellement des polygones fermés d'obstacles.
- **Validation** : minimum `3` sommets (`MIN_POLYGON_VERTICES`), rejet des auto-intersections et des sommets dupliqués à moins de `0,5 m` (`MIN_VERTEX_DISTANCE_M`).
- **Projection** : projection locale ENU centrée sur le polygone, en mètres, pour le rendu `Canvas` (`MAP_PROJECTION = LOCAL_ENU`).
- **Aucun polygone N7 n'est codé en dur** : la zone est créée en mode cartographie, ou importée depuis un plan N7 si l'utilisateur en fournit un.

## 6. Configuration & Constantes

Toutes les valeurs ci-dessous sont des valeurs par défaut, centralisées dans `GameConfig` et modifiables sans changer la logique. Les tests doivent pouvoir surcharger la configuration.

### 6.1 Géométrie / GPS / Carte
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `EARTH_RADIUS_M` | 6 371 000 | Rayon terrestre Haversine |
| `MIN_POLYGON_VERTICES` | 3 | Minimum de sommets d'un polygone |
| `MIN_VERTEX_DISTANCE_M` | 0,5 | Distance minimale entre deux sommets |
| `GPS_ACCURACY_MAX_M` | 20 | Précision GPS maximale acceptée |
| `GPS_UPDATE_INTERVAL_MS` | 1 000 | Intervalle de mise à jour GPS |
| `GPS_MIN_DISTANCE_M` | 1 | Distance minimale entre deux fixes |
| `GPS_SMOOTHING_ENABLED` | false | Lissage GPS (V1 brut) |
| `CARTOGRAPHY_SAMPLE_DISTANCE_M` | 3 | Échantillonnage en marche |
| `CARTOGRAPHY_SAMPLE_INTERVAL_MS` | 2 000 | Échantillonnage temporel |
| `DOUGLAS_PEUCKER_TOLERANCE_M` | 1 | Simplification du polygone |
| `CARTOGRAPHY_UNDO_ENABLED` | true | Undo en édition |
| `MAP_PROJECTION` | LOCAL_ENU | Projection Canvas |

### 6.2 Capteurs
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `CAMERA_BLUE_FILTER_ALPHA` | 0,15 | Filtre bleu/IR |
| `CAMERA_GLITCH_DURATION_MS` | 500 | Durée du glitch |
| `COMPASS_LOW_PASS_ALPHA` | 0,15 | Lissage boussole |
| `COMPASS_FALLBACK_ENABLED` | true | Fallback magnétomètre |
| `COMPASS_IGNORE_UNRELIABLE` | true | Ignorer les mesures faibles |
| `NOISE_WINDOW_MS` | 200 | Fenêtre RMS |
| `NOISE_SMOOTHING_ALPHA` | 0,2 | Lissage bruit |
| `NOISE_THRESHOLD_DBFS` | -30 | Seuil de furtivité |
| `NOISE_HYSTERESIS_S` | 2 | Hystérésis |
| `NOISE_ALERT_HOLD_S` | 3 | Maintien de l'alerte |
| `NOISE_GAUGE_MIN_DBFS` | -60 | Jauge bruit min |
| `NOISE_GAUGE_MAX_DBFS` | 0 | Jauge bruit max |
| `TILT_DEADZONE_DEG` | 5 | Deadzone tilt |
| `TILT_GAIN` | 1,0 | Gain tilt |
| `TILT_CLAMP_DEG` | 30 | Clamp tilt |
| `ACCEL_SAMPLING_HZ` | 50 | Fréquence accéléromètre |

### 6.3 Gameplay
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `TICK_MS` | 100 | Pas de simulation |
| `ZONE_TRIGGER_PROBABILITY_PER_S` | 0,02 | Proba d'anomalie |
| `ZONE_TRIGGER_COOLDOWN_S` | 30 | Cooldown après tracking |
| `ZONE_POLYGON_MIN_VERTICES` | 5 | Zone hantée aléatoire |
| `ZONE_POLYGON_MAX_VERTICES` | 8 | Zone hantée aléatoire |
| `ZONE_RADIUS_MIN_M` | 10 | Rayon min zone |
| `ZONE_RADIUS_MAX_M` | 30 | Rayon max zone |
| `ADMIN_ZONE_DEFAULT_RADIUS_M` | 25 | Cercle admin |
| `GHOST_SPAWN_MIN_DISTANCE_M` | 15 | Spawn min |
| `GHOST_SPAWN_MAX_DISTANCE_M` | 40 | Spawn max |
| `GHOST_BASE_SPEED_MPS` | 1,2 | Vitesse de base |
| `GHOST_FLEE_MULTIPLIER` | 2,0 | Fuite bruit |
| `MINE_TRIGGER_RADIUS_M` | 3,0 | Déclenchement mine |
| `MINE_STUN_DURATION_S` | 10 | Stun mine |
| `MINE_MAX_ACTIVE` | 3 | Mines actives max |
| `MINE_MIN_DISTANCE_M` | 5,0 | Distance entre mines |
| `LURE_DURATION_S` | 8 | Durée appât |
| `LURE_SPEED_MPS` | 1,5 | Vitesse attraction |
| `CAPTURE_DISTANCE_M` | 4,0 | Distance de capture |
| `CAPTURE_REQUIRED_SECONDS` | 5,0 | Temps cumulé |
| `CAPTURE_ANGLE_TOLERANCE_DEG` | 12,0 | Tolérance faisceau |
| `CAPTURE_TIMEOUT_S` | 30,0 | Timeout capture |
| `CAPTURE_PROGRESS_DECAY_PER_S` | 0,0 | Pas de décroissance |
| `CAPTURE_FORCE_CHANGE_MIN_S` | 0,5 | Force aléatoire |
| `CAPTURE_FORCE_CHANGE_MAX_S` | 1,5 | Force aléatoire |
| `CAPTURE_FORCE_AMPLITUDE_DEG` | 20 | Amplitude force |
| `SCREAMER_DELAY_MIN_S` | 1,0 | Délai screamer |
| `SCREAMER_DELAY_MAX_S` | 2,0 | Délai screamer |
| `SCREAMER_DURATION_S` | 2,0 | Durée screamer |
| `SCREAMER_ENABLED` | true | Activer/désactiver le screamer |
| `SCREAMER_SKIPPABLE` | false | Non skippable |
| `PLAYER_OUT_OF_AREA_PAUSE` | true | Pause hors zone |

### 6.4 Progression
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `STARTING_MINE_COUNT` | 1 | Inventaire initial |
| `STARTING_LURE_COUNT` | 1 | Inventaire initial |
| `LOOTBOX_PER_CAPTURE` | 1 | Récompense |
| `LOOT_MINE_WEIGHT` | 45 | Tirage lootbox |
| `LOOT_LURE_WEIGHT` | 45 | Tirage lootbox |
| `LOOT_BEAM_WEIGHT` | 10 | Tirage lootbox |
| `PITY_ENABLED` | true | Pity configurable |
| `PITY_MISSES_BEFORE_GUARANTEE` | 10 | Seuil pity |
| `PITY_GUARANTEE_TYPE` | BEAM_UPGRADE | Récompense garantie |
| `BEAM_MAX_LEVEL` | 5 | Niveaux faisceau |
| `BEAM_BASE_REQUIRED_SECONDS` | 5,0 | Temps niveau 1 |
| `BEAM_TIME_REDUCTION_PER_LEVEL_S` | 0,5 | Réduction temps |
| `BEAM_MIN_REQUIRED_SECONDS` | 2,0 | Temps minimum |
| `BEAM_BASE_ANGLE_TOLERANCE_DEG` | 12,0 | Tolérance niveau 1 |
| `IDENTITY_COUNT` | 10 | Fantômedex |
| `IDENTIFICATION_FREQ_MIN_HZ` | 1 | Mini-jeu |
| `IDENTIFICATION_FREQ_MAX_HZ` | 10 | Mini-jeu |
| `IDENTIFICATION_TOLERANCE_HZ` | 0,5 | Mini-jeu |
| `IDENTIFICATION_ATTEMPTS` | 3 | Mini-jeu |
| `IDENTIFICATION_RETRY_ALLOWED` | true | Réessai |
| `IDENTITY_HAS_GAMEPLAY_EFFECT` | false | Cosmétique |

**Table initiale des niveaux de faisceau** (modifiable) :

| Niveau | Temps requis | Tolérance angulaire |
|---:|---:|---:|
| 1 | 5,0 s | ±12° |
| 2 | 4,5 s | ±12° |
| 3 | 4,0 s | ±14° |
| 4 | 3,5 s | ±14° |
| 5 | 3,0 s | ±16° |

### 6.5 V2 / Étages
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `PRESSURE_PER_FLOOR_HPA` | 0,36 | Pression par étage |
| `FLOOR_HYSTERESIS_HPA` | 0,1 | Hystérésis |
| `BAROMETER_CALIBRATION_S` | 5 | Calibration |
| `FLOOR_GROUND` | 0 | RDC |
| `FLOOR_MIN` | 0 | Pas de sous-sol |

### 6.6 Interface, radar & drapeaux de règles
| Constante | Valeur par défaut | Rôle |
|---|---:|---|
| `RADAR_RANGE_M` | 100 | Portée du radar |
| `RADAR_ORIENTATION` | PHONE_UP | Orientation du radar |
| `RADAR_SHOW_DISTANCE` | true | Distance chiffrée |
| `RADAR_VISIBLE_STATES` | TRACKING | Visibilité radar |
| `ADMIN_SPAWN_AT_MAP_POINT` | true | Spawn admin au point carte |
| `ADMIN_IGNORE_ZONES_AND_PROBABILITY` | true | Spawn admin direct |
| `ADMIN_DEBUG_ONLY` | true | Admin en debug uniquement |
| `CAMERA_GLITCH_ON_SPAWN` | true | Glitch à l'apparition |
| `CAMERA_GLITCH_ON_CAPTURE` | true | Glitch à la capture |
| `SCREAMER_VIBRATION_ENABLED` | true | Vibration screamer |
| `CAPTURE_PAUSE_ON_LOCK_LOST` | true | Pause du cumul |
| `CAPTURE_GHOST_POSITION_LOCKED` | true | Fantôme ancré |
| `NOISE_AFFECTS_CAPTURE` | false | Bruit inactif en capture |
| `GADGETS_USABLE_DURING_CAPTURE` | false | Gadgets inactifs en capture |
| `MINE_ONE_SHOT` | true | Mine one-shot |
| `MINE_PERSIST_UNTIL_TRIGGERED` | true | Mine persistante |
| `MINE_VISIBLE_ON_MAP` | true | Mine visible |
| `MINE_ALERT_ON_STUN` | true | Alerte de stun |
| `LURE_PRIORITY_OVER_FLEE` | true | Priorité appât |
| `LURE_ONE_SHOT` | true | Appât one-shot |
| `LURE_AFFECTS_STUNNED` | false | Appât sans effet si stun |
| `UI_LANGUAGE` | FRENCH | Langue de l'interface |
| `ORIENTATION` | PORTRAIT | Orientation |
| `PAUSE_ON_BACKGROUND` | true | Pause arrière-plan |
| `SOUND_SOURCE` | SYSTEM_DEFAULT | Sons système |
| `VIBRATION_ENABLED` | true | Vibrations |
| `ASSETS_OPTIONAL` | true | Assets remplaçables, non requis en V1 |

## 7. Persistance & Modèle de Données

- **À persister** : polygone extérieur, polygones d'obstacles, zones hantées, inventaire, niveau du faisceau, Fantômedex, mines posées. L'état de session en cours (fantôme actif, capture en cours) n'est pas persisté.
- **Sérialisation** : Gson (`com.google.code.gson:gson`) est la seule dépendance de sérialisation autorisée. Elle est compatible avec les tests JUnit purs et adaptée aux polygones imbriqués et au Fantômedex.
- **Stockage Android** : `SharedPreferences` côté couche Android (`data`/`ui`), derrière une interface de dépôt définie dans `domain` (ex. `GameRepository` / `GameSave`). Le domaine ne dépend ni de `SharedPreferences` ni d'`android.*`.
- **Identités du Fantômedex** : fournies par l'utilisateur dans un fichier JSON (ou format équivalent), chargé par la couche Android et exposé au domaine sous forme d'objets purs.

## 8. Extensibilité V2

- `GameInput` contient :
  - `int floor` (0 par défaut),
  - `Double pressureHpa` (nullable, utilisé en V2).
- Une seule interface `FloorProvider` est créée en J1 ; elle retourne `0` en V1. Pas de `WorldMap`, `FloorMap` ni `Floor` en J1.
- En V2 :
  - le baromètre détermine l'étage courant,
  - les zones hantées sont définies par des coordonnées sur la carte,
  - le fantôme peut apparaître sur plusieurs étages,
  - la V1 ignore totalement l'altitude.
- Les règles de géométrie, de déplacement et de capture doivent rester indépendantes de l'étage pour être réutilisées en V2.

## 9. Décisions Techniques Figées

- Java 11, Android natif, vues XML, `Canvas` 2D, pas de Jetpack Compose.
- `minSdk 26`, `targetSdk 37`, `compileSdk 37`.
- Domaine 100 % Java pur, aucun import `android.*`, testable avec JUnit 4.
- Dépendances autorisées : CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`), Play Services Location, et Gson pour la sérialisation. Toute autre dépendance doit être validée.
- Pas de `WorldMap`, `FloorMap`, `Floor` en J1 ; `FloorProvider` retourne 0.
- Tick fixe de 100 ms, `GameClock` et `RandomSource` injectables (seed) pour des tests déterministes.
- Orientation portrait, pause en arrière-plan.
- Interface en français.
- Sons et vibrations : sons système par défaut ; assets remplaçables sans impact sur les règles métier.

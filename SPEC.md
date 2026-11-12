# Cahier des Charges - Ghost Radar

## 1. Concept & Ambiance
Application mobile parodique de chasse aux fantômes sur le campus (N7). L'écran principal affiche le flux vidéo de la caméra arrière en temps réel avec un habillage façon caméscope d'enquête paranormale / Ghostbusters, conçu pour être filmé en enregistrement d'écran.

## 2. Capteurs & Rôles

### Périmètre V1 (Extérieur - Cour / Campus) — 5 capteurs
1. **Caméra arrière (`CameraX PreviewView`)** :
   - Arrière-plan plein écran permanent.
   - Superposition d'un filtre visuel bleu/infrarouge léger et d'effets de parasites/glitchs lors de l'apparition ou de la capture d'un fantôme.
2. **GPS (`FusedLocationProviderClient`)** :
   - Cartographie du périmètre jouable (polygone lat/lon) et des zones hantées.
   - Suivi temps réel du joueur et déplacement du fantôme contraint à l'intérieur du polygone.
3. **Boussole (`Sensor.TYPE_ROTATION_VECTOR` ou Magnétomètre)** :
   - Orientation en direct du capteur fantasmagorique (radar) selon l'azimut du téléphone.
4. **Microphone (`AudioRecord`)** :
   - Mesure continue du niveau sonore ambiant (RMS).
   - Furtivité : si le bruit dépasse un seuil défini, le fantôme repère le joueur et sa vitesse de fuite double.
5. **Accéléromètre (`Sensor.TYPE_ACCELEROMETER`)** :
   - Contrôle du viseur pendant la phase de capture : le fantôme tire dans des directions aléatoires et le joueur incline le téléphone en sens inverse pour maintenir le faisceau verrouillé.

### Périmètre V2 (Extension Intérieur & Étages) — 6ᵉ capteur
6. **Baromètre (`Sensor.TYPE_PRESSURE`)** :
   - Calibrage d'une pression de référence $P_0$ au rez-de-chaussée.
   - Détection automatique de l'étage actuel du joueur ($\approx 0.36\text{ hPa}$ par étage) permettant de traquer des fantômes répartis sur plusieurs niveaux du bâtiment.

## 3. Interface Utilisateur (Écran Principal & Navigation)
- **Fond** : Flux caméra arrière plein écran + calque filtre bleu/infrarouge.
- **HUD Caméscope minimaliste (en haut)** :
  - Indicateur `● REC` (point rouge clignotant) + horloge temps réel (`HH:MM:SS`) + jauge discrète de bruit ambiant (micro).
- **Boutons d'action principaux (en bas / côtés)** :
  1. **Bouton `Capteur Fantasmagorique`** : affiche ou masque en overlay (façon capteur cardiaque Warzone) le radar orienté par la boussole indiquant la distance et la direction du fantôme.
  2. **Bouton `Map`** : ouvre la vue carte 2D montrant le polygone cartographié, la position du joueur, les zones hantées et les mines posées. Permet aussi d'accéder au **Mode Cartographie** (acquisition GPS automatique en marchant + édition manuelle des sommets du polygone).
  3. **Bouton `Inventaire`** : accès aux gadgets consommables, aux améliorations du faisceau, aux Lootboxes à ouvrir et au **Fantômedex**.
- **Mode Admin / Debug (bouton discret dans un coin)** :
  - Poser une "Zone Hantée" à la position GPS actuelle.
  - Forcer l'apparition immédiate d'un fantôme à proximité (bouton de secours pour la démonstration).

## 4. Boucle de Jeu (Machine à états)
1. **`EXPLORATION`** : Le joueur se déplace dans la zone cartographiée. Lorsqu'il entre dans une Zone Hantée, une probabilité par seconde déclenche une alerte plein écran : `"ANOMALIE DÉTECTÉE"`.
2. **`TRACKING`** : Le fantôme est actif et se déplace dans le polygone (observable sur le Capteur Fantasmagorique). Il fuit le joueur (2x plus vite si le micro détecte du bruit).
   - *Gadgets actifs en tracking* : **Mine à ectoplasme** (posée au GPS, immobilise le fantôme 10s s'il passe à < 3m) et **Appât sonore** (attire le fantôme vers le joueur pendant 8s).
3. **`SCREAMER` -> `CAPTURE`** :
   - Quand `distance < 4m`, le point disparaît du radar. Après 1 à 2 secondes, un **Screamer** (image plein écran + son + vibration) s'affiche, puis le combat démarre par-dessus la caméra.
   - Le joueur active son **Faisceau** et compense les mouvements du fantôme à l'**Accéléromètre** pour maintenir le viseur sur la cible pendant 5 secondes cumulées.
4. **`RECOMPENSE` & `FANTOMEDEX`** :
   - Chaque capture rapporte 1 **Caisse Paranormale** (tirage façon CS:GO : Mines, Appâts, ou amélioration de niveau du Faisceau pour réduire le temps nécessaire / les mouvements nécessaires à la capture).
   - Le fantôme rejoint le **Fantômedex** en tant qu'"Entité Non Identifiée" ; réussir un court **mini-jeu d'identification** (ex: calibrage de fréquence) révèle son identité parodique (nom de prof/cours, rareté, description).

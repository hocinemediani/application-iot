# Ghost Radar - Directives Agent

## Commandes de vérification
- Lancer les tests unitaires : `./gradlew test`
- Compiler l'APK debug : `./gradlew assembleDebug`

## Stack et Architecture
- **Langage & UI** : Java (Android natif), vues XML classiques (pas de Jetpack Compose) + `Canvas` 2D natif pour le radar et l'éditeur de carte.
- **Build** : Gradle Kotlin DSL (`build.gradle.kts`) avec Version Catalog (`gradle/libs.versions.toml`).
- **Package racine** : `com.example.ghostradar`
- **Séparation stricte** :
  - `com.example.ghostradar.domain` : toute la logique métier (machine à états, géométrie du polygone, calculs GPS, seuils sonores, physique de capture, lootboxes, gestionnaire d'étages) en **Java pur** (aucun import `android.*`) pour être 100 % testable avec JUnit.
  - `com.example.ghostradar.sensors` : interfaces pour chaque capteur (GPS, Boussole, Micro, Accéléromètre, et Baromètre prévu pour V2) et leurs implémentations Android séparées.
  - `com.example.ghostradar.ui` : Activities/Fragments, vues XML et flux vidéo CameraX (`PreviewView`).

## Règles de travail
1. **TDD obligatoire** : toujours rédiger les tests unitaires JUnit dans `app/src/test/java/com/example/ghostradar/` AVANT d'implémenter la logique métier correspondante.
2. **Dépendances** : seule l'ajout de **CameraX** (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) et **Play Services Location** est pré-approuvé si absent. Demande validation avant toute autre dépendance.
3. **Anti-régression** : exécuter `./gradlew test` après chaque modification de code.
4. **Traçabilité** : à la fin de chaque tâche validée, ajoute une entrée datée dans `JOURNAL.md` (fichiers créés/modifiés, tests ajoutés, commandes exécutées).
5. **Zéro supposition sur les ambiguïtés** : si une règle de `SPEC.md` est imprécise, manque de valeurs chiffrées (ex. seuils, durées, tailles) ou offre plusieurs choix d'implémentation, **arrête-toi et pose tes questions numérotées avant d'écrire la moindre ligne de code**.
6. **Journalisation automatique** : avant de conclure toute réponse où des fichiers ont été créés ou modifiés, ajoute obligatoirement une nouvelle entrée datée en bas de `JOURNAL.md` en respectant strictement le modèle défini en haut de ce fichier.

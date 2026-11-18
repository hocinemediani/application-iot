# Ghost Radar - Directives Agent

## Démarrage de session (obligatoire)

- Lire `PLAN.md` puis `SPEC.md` avant toute action : `PLAN.md` donne l'étape en cours, `SPEC.md` les règles et toutes les valeurs numériques.
- L'état d'avancement fait foi dans `PLAN.md` ; ne pas se fier à l'arborescence (le domaine peut être vide).
- Si une règle de `SPEC.md` est imprécise, non chiffrée ou offre plusieurs choix, s'arrêter et poser des questions numérotées avant d'écrire du code.

## Commandes (Windows / PowerShell)

- Sur cette machine, `java` n'est pas dans le PATH : préfixer chaque commande Gradle par `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr';` et utiliser `.\gradlew.bat`, pas `./gradlew`.
- Tests unitaires : `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat test`
- APK debug : `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat assembleDebug`
- Test unique : `$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'; .\gradlew.bat :app:testDebugUnitTest --tests "com.example.ghostradar.domain.geo.GeoMathTest"`
- `local.properties` (gitignored, absent après un clone) doit définir `sdk.dir`, ou `ANDROID_HOME` doit être défini ; sinon le build échoue avec `SDK location not found`. SDK local : `C:\Users\ysala\AppData\Local\Android\Sdk`.
- Le premier build peut installer automatiquement la plateforme Android 37 si les licences sont acceptées.
- Aucun lint, formatter ou CI personnalisé n'est configuré ; la vérification de référence est `test`, complétée par `assembleDebug` après une modification d'UI.
- Pour démarrer/reprendre le Jalon 1 : commande OpenCode `/jalon1 [périmètre]` (`.opencode/commands/jalon1.md`).

## Stack et build

- Java 11, Android natif, vues XML, `Canvas` 2D ; **pas de Jetpack Compose**, et Kotlin uniquement pour le DSL Gradle.
- Package racine `com.example.ghostradar` ; Gradle 9.6.0, AGP 9.5.1, `minSdk 26`, `compileSdk/targetSdk 37`.
- Dépendances via le Version Catalog `gradle/libs.versions.toml` ; ne pas coder les versions en dur. Le DSL AGP 9 est déjà en place (`compileSdk { version = release(37) }`) : ne pas le remplacer par l'ancienne syntaxe.
- Dépendances pré-approuvées : CameraX, Play Services Location et Gson (sérialisation uniquement). Toute autre dépendance doit être validée avant ajout.

## Architecture et règles non négociables

- `com.example.ghostradar.domain` : logique métier en **Java pur**, aucun import `android.*`, testable en JUnit 4 sur la JVM (pas d'émulateur).
- `com.example.ghostradar.sensors` : interfaces capteurs et implémentations Android séparées ; `com.example.ghostradar.ui` : Activities/Fragments, XML et CameraX.
- TDD strict : écrire les tests dans `app/src/test/java/com/example/ghostradar/` **avant** d'implémenter la classe correspondante.
- Toutes les constantes passent par `GameConfig` (valeurs dans `SPEC.md` §6), aucun nombre magique dispersé. Pas de valeur de `SPEC.md` modifiée sans validation.
- Tick fixe de 100 ms ; `GameClock` et `RandomSource` sont injectables (seed) pour des tests déterministes.
- V1 : `GameInput.floor = 0`, `pressureHpa` nullable, `FloorProvider` retourne 0. Ne pas créer `WorldMap`, `FloorMap` ni `Floor`.
- UI entièrement en français, portrait verrouillé, simulation en pause lorsque l'application passe en arrière-plan.
- Persistance : Gson uniquement, et `SharedPreferences` côté Android derrière une interface de dépôt définie dans `domain`.

## Traçabilité (obligatoire)

- Lancer `test` après chaque modification de code ; lancer `assembleDebug` après une modification d'UI.
- Avant de conclure une intervention qui a créé ou modifié des fichiers : cocher les sous-tâches terminées dans `PLAN.md`, actualiser `Dernière étape validée` et `Prochaine action immédiate`, puis ajouter une entrée datée en bas de `JOURNAL.md` au format défini en haut du fichier.

---
description: Lance ou reprend le Jalon 1 du domaine en TDD
---

Tu travailles sur Ghost Radar. Périmètre demandé : $ARGUMENTS

Si aucun périmètre n'est fourni, prends la première sous-tâche non cochée du Jalon 1 dans `PLAN.md`.

Avant de coder :
1. Lis `AGENTS.md`, puis `PLAN.md` et les sections de `SPEC.md` liées au périmètre.
2. Vérifie que `local.properties` contient `sdk.dir` (ou que `ANDROID_HOME` est défini) et que `JAVA_HOME` pointe vers un JDK ; sinon le build échoue avec `SDK location not found`.
3. Si une règle ou une valeur numérique manque dans `SPEC.md`, arrête-toi et pose des questions numérotées avant d'écrire du code.

Boucle de travail obligatoire (TDD) :
1. Écris d'abord les tests JUnit 4 dans `app/src/test/java/com/example/ghostradar/`.
2. Implémente ensuite les classes dans `com.example.ghostradar.domain` en Java pur, sans aucun import `android.*`.
3. Centralise toutes les valeurs dans `GameConfig` ; aucun nombre magique dispersé dans le code.
4. Lance `.\gradlew.bat test` après chaque lot test → code. En PowerShell, si `java` n'est pas dans le PATH, définis `JAVA_HOME` vers le JBR d'Android Studio (`C:\Program Files\Android\Android Studio\jbr`).
5. Corrige jusqu'à ce que les tests soient verts.

À la fin :
- Coche les sous-tâches réellement terminées dans `PLAN.md`.
- Actualise `Dernière étape validée` et `Prochaine action immédiate`.
- Ajoute une entrée datée dans `JOURNAL.md` en respectant le format défini en haut du fichier.

Poursuis jusqu'à ce que le périmètre demandé soit implémenté et testé ; ne t'arrête pas après l'écriture des tests.

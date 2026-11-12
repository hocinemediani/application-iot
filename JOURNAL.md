# Journal de Bord - Ghost Radar (Vibe Coding)

## Environnement & Outillage
- **Agent CLI** : OpenCode
- **Stack** : Android Studio, Java, Vues XML + Canvas 2D natif, Gradle Kotlin DSL (`build.gradle.kts`)

## Format obligatoire des entrées (à ajouter à la fin du fichier par l'agent)
### [YYYY-MM-DD HH:MM] - Titre court de la tâche
- **Prompt / Demande** : résumé de la consigne reçue
- **Fichiers créés / modifiés** : liste des chemins
- **Tests exécutés** : résultat de `./gradlew test`
- **Décisions / Points d'attention** : choix techniques ou limites rencontrées

---

### [2026-11-12 20:53] - Cadrage complet des spécifications V1 (51 questions)
- **Prompt / Demande** : Lire `AGENTS.md`, `SPEC.md` et `app/build.gradle.kts`, proposer un découpage en 4 jalons et la liste des classes Java du Jalon 1 dans `com.example.ghostradar.domain`, puis poser toutes les questions de seuils/règles/ambiguïtés avant d'écrire du code.
- **Fichiers créés / modifiés** : `SPEC.md` (réécriture complète avec les 51 réponses et les constantes), `PLAN.md` (création), `AGENTS.md` (ajout de la section `Continuité inter-sessions (obligatoire)` + mise à jour de la règle 2 pour autoriser Gson), `JOURNAL.md` (présente entrée).
- **Tests exécutés** : aucun test de code exécuté (phase de cadrage, aucune ligne de code métier écrite).
- **Décisions / Points d'attention** : 51 réponses intégrées ; domaine J1 100 % Java pur ; suppression de `WorldMap`, `FloorMap` et `Floor` du J1 ; `FloorProvider` retourne 0 en V1 ; `GameInput` expose `floor = 0` et `pressureHpa` nullable ; GPS brut en V1 ; carte composée d'un polygone extérieur et de polygones d'obstacles ; zones hantées polygonales aléatoires / cercle admin ; capture avec fantôme ancré et capture possible pendant le stun ; Gson autorisé comme unique dépendance de sérialisation (à ajouter en J4) ; persistance via `SharedPreferences` derrière une interface `domain`. Quelques valeurs non chiffrées dans les réponses (centre/rayon des zones aléatoires, poids du pity, table des niveaux de faisceau, alpha de lissage bruit) ont été figées en valeurs initiales configurables dans `SPEC.md`.
- **Reste à faire** : écrire les tests JUnit du Jalon 1 avant toute implémentation, puis créer les classes `domain` listées dans `PLAN.md`.



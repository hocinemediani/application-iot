package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.Validation;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Champ de mines à ectoplasme.
 *
 * <p>Une mine se pose à la position GPS du joueur, uniquement dans la zone
 * jouable, à au moins {@code MINE_MIN_DISTANCE_M} des autres mines actives et
 * dans la limite de {@code MINE_MAX_ACTIVE}. Elle persiste jusqu'au
 * déclenchement : si le fantôme passe à moins de
 * {@code MINE_TRIGGER_RADIUS_M}, la mine la plus proche se déclenche,
 * immobilise le fantôme {@code MINE_STUN_DURATION_S} et libère son slot. Une
 * mine déclenchée reste visible sur la carte pendant son stun, puis disparaît ;
 * {@link #getActiveStun(Ghost)} identifie celle qui a provoqué le stun en cours
 * avec le temps restant. Le déclenchement est signalé par le retour de
 * {@link #update(Ghost)} ; la couche UI décide de l'alerte ou du son.</p>
 *
 * <p>Les flags {@code MINE_ONE_SHOT} et {@code MINE_PERSIST_UNTIL_TRIGGERED}
 * sont documentés sans variante de comportement en V1 : le champ reste one-shot
 * et les mines persistent jusqu'au déclenchement, quelle que soit leur valeur.</p>
 */
public final class EctoplasmMine {

    private final GameConfig config;
    private final PlayArea playArea;
    private final List<PlacedMine> mines = new ArrayList<>();
    private PlacedMine stunningMine;

    public EctoplasmMine(GameConfig config, PlayArea playArea) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (playArea == null) {
            throw new IllegalArgumentException("playArea must not be null");
        }
        Validation.requireSameRadius("earthRadiusM (config/playArea)",
                config.getEarthRadiusM(), playArea.getEarthRadiusM());
        if (config.getMineTriggerRadiusM() <= 0.0) {
            throw new IllegalArgumentException("mineTriggerRadiusM must be positive");
        }
        if (config.getMineStunDurationS() <= 0.0) {
            throw new IllegalArgumentException("mineStunDurationS must be positive");
        }
        if (config.getMineMaxActive() <= 0) {
            throw new IllegalArgumentException("mineMaxActive must be positive");
        }
        if (config.getMineMinDistanceM() < 0.0) {
            throw new IllegalArgumentException("mineMinDistanceM must not be negative");
        }
        this.config = config;
        this.playArea = playArea;
    }

    /**
     * Pose une mine à la position du joueur.
     *
     * @return {@code true} si la position est jouable, assez éloignée des
     *         autres mines actives et sous le maximum de mines actives
     */
    public boolean place(LatLon playerPosition) {
        if (playerPosition == null) {
            throw new IllegalArgumentException("playerPosition must not be null");
        }
        if (getActiveCount() >= config.getMineMaxActive()) {
            return false;
        }
        if (!playArea.contains(playerPosition)) {
            return false;
        }

        for (PlacedMine mine : mines) {
            if (mine.isTriggered()) {
                continue;
            }
            double distanceM = GeoMath.haversineDistanceMeters(
                    playerPosition, mine.getPosition(), config.getEarthRadiusM());
            if (distanceM < config.getMineMinDistanceM()) {
                return false;
            }
        }

        mines.add(new PlacedMine(playerPosition));
        return true;
    }

    /**
     * Teste le déclenchement des mines au tick courant.
     *
     * <p>Au plus une mine se déclenche par appel : la plus proche du fantôme
     * parmi les mines non déclenchées situées à moins de
     * {@code MINE_TRIGGER_RADIUS_M}. Un fantôme déjà stunné ignore toutes les
     * mines (Q1) : aucune mine n'est consommée et le timer de stun n'est pas
     * remis à zéro, même si le fantôme se trouve dans la zone de plusieurs
     * mines à 5,1 m l'une de l'autre.</p>
     *
     * @return {@code true} si une mine vient de se déclencher
     */
    public boolean update(Ghost ghost) {
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        refreshStun(ghost);
        if (ghost.isStunned()) {
            return false;
        }

        PlacedMine nearest = null;
        double nearestDistanceM = Double.MAX_VALUE;
        for (PlacedMine mine : mines) {
            if (mine.isTriggered()) {
                continue;
            }
            double distanceM = GeoMath.haversineDistanceMeters(
                    ghost.getPosition(), mine.getPosition(), config.getEarthRadiusM());
            if (distanceM < config.getMineTriggerRadiusM() && distanceM < nearestDistanceM) {
                nearestDistanceM = distanceM;
                nearest = mine;
            }
        }

        if (nearest == null) {
            return false;
        }

        nearest.triggered = true;
        stunningMine = nearest;
        ghost.stun(config.getMineStunDurationS());
        return true;
    }

    /**
     * Nombre de mines encore actives, c'est-à-dire non déclenchées.
     */
    public int getActiveCount() {
        int count = 0;
        for (PlacedMine mine : mines) {
            if (!mine.isTriggered()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Mines à afficher sur la carte : toutes les mines actives (non
     * déclenchées), plus la mine qui a provoqué le stun en cours tant qu'il
     * dure. Les mines déclenchées lors d'un stun précédent ne réapparaissent
     * pas.
     *
     * @return liste non modifiable, vide si {@code MINE_VISIBLE_ON_MAP = false}
     */
    public List<PlacedMine> getVisibleMines(Ghost ghost) {
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        refreshStun(ghost);
        if (!config.isMineVisibleOnMap()) {
            return Collections.emptyList();
        }

        List<PlacedMine> visible = new ArrayList<>();
        if (stunningMine != null) {
            visible.add(stunningMine);
        }
        for (PlacedMine mine : mines) {
            if (!mine.isTriggered()) {
                visible.add(mine);
            }
        }
        return Collections.unmodifiableList(visible);
    }

    /**
     * Mine à l'origine du stun en cours, avec le temps de stun restant, afin
     * que l'UI puisse l'afficher différemment (halo, alerte) pendant le stun.
     *
     * @return vide si le fantôme n'est pas stunné ou si le stun ne provient pas
     *         d'une mine de ce champ
     */
    public Optional<ActiveStun> getActiveStun(Ghost ghost) {
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        refreshStun(ghost);
        if (stunningMine == null) {
            return Optional.empty();
        }
        return Optional.of(new ActiveStun(stunningMine, ghost.getStunRemainingS()));
    }

    private void refreshStun(Ghost ghost) {
        if (!ghost.isStunned()) {
            stunningMine = null;
        }
    }

    /**
     * Mine déclenchée à l'origine du stun en cours et temps restant.
     */
    public static final class ActiveStun {

        private final PlacedMine mine;
        private final double remainingS;

        private ActiveStun(PlacedMine mine, double remainingS) {
            this.mine = mine;
            this.remainingS = remainingS;
        }

        public PlacedMine getMine() {
            return mine;
        }

        public double getRemainingS() {
            return remainingS;
        }
    }

    /**
     * Mine posée, avec sa position et son état.
     */
    public static final class PlacedMine {

        private final LatLon position;
        private boolean triggered;

        private PlacedMine(LatLon position) {
            this.position = position;
        }

        public LatLon getPosition() {
            return position;
        }

        public boolean isTriggered() {
            return triggered;
        }
    }
}

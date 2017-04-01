package gameEngine;

import controllers.ChooseCarController;
import dataHandler.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import keyHandler.KeyHandlerOnPress;
import keyHandler.KeyHandlerOnRelease;
import models.*;
import music.MusicPlayer;
import stageHandler.StageManager;
import stageHandler.StageManagerImpl;
import constants.Constants;

import java.util.Observable;
import java.util.Observer;

public class RunTrack {

    private static long time;
    private static boolean isPaused;
    private static float velocity;
    private static boolean shoot;
    private static CurrentStats currentStats;
    private static Cheat cheat;
    private int frame;
    private int y;
    private float currentFramesPerSecond;
    private String carId;
    private Player player;
    private CurrentHealth currentHealth;
    private ChooseCarController chooseCarController;
    private Collectible collectible;
    private Obstacle obstacle;
    private Ammo ammo;

    public static Cheat getCheat() {
        return cheat;
    }

    public RunTrack(Player player, float velocityValue) {
        frame = 0;
        time = 0;
        isPaused = false;
        shoot = false;
        velocity = velocityValue;
        currentStats = new CurrentStats(0, 0, 0, 0);
        cheat = new Cheat();
        this.collectible = new Collectible(player);
        this.obstacle = new Obstacle();
        this.ammo = new Ammo();
        this.chooseCarController = new ChooseCarController();
        this.setCarId(chooseCarController.getCarId());
        this.setPlayer(player);
        this.setCurrentFramesPerSecond(Constants.FRAMES_PER_SECOND);
    }

    public void runGame(AnchorPane root, Image background, int drunkDrivers, int minLeftSide, int maxRightSide) {

        StageManager manager = new StageManagerImpl();
        Canvas canvas = new Canvas(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);

        root.getChildren().add(canvas);
        root.getScene().setOnKeyPressed(new KeyHandlerOnPress(this.getPlayer(), minLeftSide, maxRightSide));
        root.getScene().setOnKeyReleased(new KeyHandlerOnRelease(this.getPlayer(), minLeftSide, maxRightSide));
        GraphicsContext gc = canvas.getGraphicsContext2D();

        String carImg = Constants.CAR_IMAGES_PATH + this.carId + Constants.HALF_SIZE;
        this.player.setImage(carImg);
        this.player.setPosition(200, 430);
        this.player.setPoints(0L);

        this.currentHealth = new CurrentHealth(this.player);
        currentStats.addObserver(observer);

        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        MusicPlayer.play();
        MusicPlayer.pause();
        KeyFrame kf = new KeyFrame(
                Duration.seconds(currentFramesPerSecond),
                event -> {

                    if (isPaused) {
                        PauseHandler pauseHandler = new PauseHandler(gameLoop, gc, background, y, player, obstacle.getObstacles(), collectible.getCollectibles());
                        pauseHandler.activatePause();
                    }

                    y = Math.round(y + velocity);
                    time++;
                    frame++;

                    //update immortality status if its actuvated
                    collectible.updateStatus();

                    currentStats.setTime((long) (time * currentFramesPerSecond));
                    currentStats.setDistance(currentStats.getDistance() + (long) velocity / 2);
                    player.addPoints(1);
                    currentStats.setPoints(player.getPoints());
                    currentStats.setBullets(player.getAmmunition());

                    observer.update(currentStats, observer);

                    if (Math.abs(y) >= Constants.CANVAS_HEIGHT) {
                        y = y - Constants.CANVAS_HEIGHT;
                        frame = 0;
                    }
                    this.player.setVelocity(0, 0);

                    cheat.useCheat(player);

                    //Generate obstacles
                    if (frame == 0) {
                        obstacle.addObstacle(obstacle.generateObstacle(drunkDrivers, minLeftSide, maxRightSide));
                    }

                    gc.clearRect(0, 0, Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);
                    gc.drawImage(background, 0, y - Constants.CANVAS_HEIGHT);
                    gc.drawImage(background, 0, y);
                    this.player.update();
                    this.player.render(gc);
                    currentHealth.update();

                    obstacle.manageObstacles(gc, collectible, player, obstacle.getObstacles(), velocity);

                    // Ammo logic
                    ammo.visualizeAmmo(gc, obstacle.getObstacles(), ammo.getAmmunition(),player);
                    if (shoot) {
                        ammo.addAmmo(ammo.generateAmmo(player));
                        setShoot(false);
                    }

                    Stage currentStage = (Stage) canvas.getScene().getWindow();

                    //CHECK FOR END && CHECK FOR LOSE
                    if (time >= Constants.TRACK_1_END_TIME || player.getHealthPoints() <= 0) {
                        boolean win = player.getHealthPoints() > 0 && currentStats.getDistance() >= Constants.TRACK_1_END_DISTANCE;
                        if (win) {
                            this.player.setMaxLevelPassed(this.player.getMaxLevelPassed() + 1);
                            PlayerData.getInstance().updatePlayer(PlayerData.getInstance().getCurrentPlayer());
                        }

                        PlayerData.getInstance().updatePlayer(PlayerData.getInstance().getCurrentPlayer());

                        clearObstaclesAndCollectibles();
                        gameLoop.stop();
                        MusicPlayer.stop();
                        time = 0;
                        Notification.hidePopupMessage();
                        velocity = Constants.START_GAME_VELOCITY;
                        currentStats.setDistance(0);
                        root.getChildren().remove(canvas);
                        player.setAmmunition(Constants.START_GAME_BULLETS);

                        FXMLLoader loader = manager.loadSceneToStage(currentStage, win ? Constants.GAME_WIN_VIEW_PATH : Constants.GAME_OVER_VIEW_PATH);

                        this.player.updateStatsAtEnd();
                    }

                    if (frame % Constants.COLLECTIBLES_OFFSET == 0) {
                        collectible.addCollectible(Collectible.generateCollectible(minLeftSide, maxRightSide));
                    }
                    String action = collectible.visualizeCollectible(gc, velocity, currentStage);

                    if (action != null && action.equals(Constants.ARMAGEDDON_STRING)) {
                        startArmageddonsPower();
                    } else if (action != null && action.equals(Constants.FUEL_BOTTLE_STRING)) {
                        time -= Constants.FUEL_TANK_BONUS_TIME / Constants.FRAMES_PER_SECOND;
                    }
                });

        gameLoop.getKeyFrames().add(kf);
        gameLoop.playFromStart();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static CurrentStats getCurrentStats() {
        return currentStats;
    }

    public static float getVelocity() {
        return velocity;
    }

    public static void setVelocity(float v) {
        velocity = v;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void setIsPaused(boolean newValue) {
        isPaused = newValue;
    }

    public static boolean getShoot() {
        return shoot;
    }

    public static void setShoot(boolean newValue) {
        shoot = newValue;
    }

    private static Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
        }
    };

    private void setCurrentFramesPerSecond(float currentFramesPerSecond) {
        this.currentFramesPerSecond = currentFramesPerSecond;
    }

    private void setCarId(String carId) {
        this.carId = carId == null ? "car1" : carId;
    }

    private void clearObstaclesAndCollectibles() {
        collectible.getCollectibles().clear();
        obstacle.getObstacles().clear();
    }

    private void startArmageddonsPower() {
        for (Obstacle o : obstacle.getObstacles()) {
            o.handleImpactByCarPlayer(velocity);
        }
    }
}
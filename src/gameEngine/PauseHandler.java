package gameEngine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Duration;
import models.Player;
import models.sprites.Obstacle;
import models.sprites.collectibles.Collectible;
import utils.constants.GameplayConstants;
import utils.constants.GeneralConstants;
import utils.music.MusicPlayer;

public class PauseHandler {
    private Timeline gameLoop;
    private GraphicsContext gc;
    private Image background;
    private double y;
    private Player player;
    private Iterable<Obstacle> testObstacles;
    private Iterable<Collectible> collectibles;

    public PauseHandler(Timeline gameLoop, GraphicsContext gc, Image background, double y, Player player, Iterable<Obstacle> testObstacles, Iterable<Collectible> collectibles) {
        this.gameLoop = gameLoop;
        this.gc = gc;
        this.background = background;
        this.y = y;
        this.player = player;
        this.testObstacles = testObstacles;
        this.collectibles = collectibles;
    }

    public void activatePause() {
        RunTrack.setIsPaused(true);
        this.gameLoop.pause();
        if (RunTrack.isPaused()) {
            MusicPlayer.getInstance().startStopPause();

            Timeline pauseloop = new Timeline();
            pauseloop.setCycleCount(Timeline.INDEFINITE);

            KeyFrame keyFramePause = new KeyFrame(
                    Duration.seconds(GeneralConstants.FRAMES_PER_SECOND),
                    event -> {
                        if (!RunTrack.isPaused()) {
                            this.gameLoop.play();
                            MusicPlayer.getInstance().startStopPause();
                            pauseloop.stop();
                        }

                        this.gc.clearRect(GameplayConstants.CANVAS_BEGINNING, GameplayConstants.CANVAS_BEGINNING, GeneralConstants.CANVAS_WIDTH, GeneralConstants.CANVAS_HEIGHT);
                        this.gc.drawImage(this.background, GameplayConstants.CANVAS_BEGINNING, y);
                        this.gc.drawImage(this.background, GameplayConstants.CANVAS_BEGINNING, y - GeneralConstants.CANVAS_HEIGHT);
                        this.player.getCar().render(this.gc);

                        for (Collectible collectible : collectibles) {
                            collectible.render(this.gc);
                        }
                        for (Obstacle obs : this.testObstacles) {
                            obs.render(this.gc);
                        }
                    });
            pauseloop.getKeyFrames().add(keyFramePause);
            pauseloop.play();
        }
    }
}
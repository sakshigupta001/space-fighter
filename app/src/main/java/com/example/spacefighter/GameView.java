package com.example.spacefighter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {

    //boolean variable to track if the game is playing or not
    volatile boolean playing;
    //the game thread
    private Thread gameThread = null;
    //adding the player to this class
    private Player player;


    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;


    //Adding enemies object array
    private Enemy[] enemies;
    //Adding 3 enemies you may increase the size
    private int enemyCount = 1;

    //Adding friends object array
    private Friend[] friends;
    //Adding 1 friend you may increase the size
    private int friendCount = 1;


    //Adding an stars list
    private ArrayList<Star> stars = new
            ArrayList<Star>();

    //defining a boom object to display blast
    private Blast blast;


    //a screenX holder
    int screenX;
    //to count the number of Misses
    int countMisses;
    //indicator that the enemy has just entered the game screen
    boolean flag ;
    //an indicator if the game is Over
    private boolean isGameOver ;
    private int gameOverMissCount = 3;

    //the score holder
    int score;

    static MediaPlayer gameOnsound;
    final MediaPlayer killedEnemysound;
    final MediaPlayer gameOversound;

    //context to be used in onTouchEvent to cause the activity transition from GameAvtivity to MainActivity.
    Context context;

    public GameView(Context context, int screenX, int screenY) {
        super(context);

        //initializing player object
        //this time also passing screen size to player constructor
        player = new Player(context, screenX, screenY);

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        //adding 100 stars you may increase the number
        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s  = new Star(screenX, screenY);
            stars.add(s);
        }

        //initializing enemy object array
        enemies = new Enemy[enemyCount];
        for(int i=0; i<enemyCount; i++){
            enemies[i] = new Enemy(context, screenX, screenY);
        }

        //initializing boom object
        blast = new Blast(context);

        //initializing friend object array
        friends = new Friend[enemyCount];
        for(int i=0; i<friendCount; i++){
            friends[i] = new Friend(context, screenX, screenY);
        }

        this.screenX = screenX;

        countMisses = 0;

        isGameOver = false;

        //setting the score to 0 initially
        score = 0;

        //initializing the media players for the game sounds
        gameOnsound = MediaPlayer.create(context,R.raw.gameon_kabir_singh);
        killedEnemysound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOversound = MediaPlayer.create(context,R.raw.gameover);

        //starting the game music as the game starts
        gameOnsound.start();

        //initializing context
        this.context = context;
    }

    @Override
    public void run() {
        while (playing) {
            //to update the frame
            update();

            //to draw the frame
            draw();

            //to control
            control();
        }
    }

    private void update() {

        //incrementing score as time passes
        score++;

        //updating player position
        player.update();

        //setting boom outside the screen
        blast.setX(-250);
        blast.setY(-250);

        //Updating the stars with player speed
        for (Star s : stars) {
            s.update(player.getSpeed());
        }

        //updating the enemy coordinate with respect to player speed
        for(int i=0; i<enemyCount; i++){

            if(enemies[i].getX()==screenX){
                flag = true;
            }

            enemies[i].update(player.getSpeed());

            //if collision occurs with player
            if (Rect.intersects(player.getDetectCollision(), enemies[i].getDetectCollision())) {

                //displaying boom at that location
                blast.setX(enemies[i].getX());
                blast.setY(enemies[i].getY());

                //playing a sound at the collision between player and the enemy
                killedEnemysound.start();

                //moving enemy outside the left edge
                enemies[i].setX(-300);
            } // the condition where player misses the enemy
            else {
                //if the enemy has just entered
                if(flag){
                    //if player's x coordinate is more than the enemies's x coordinate.i.e. enemy has just passed across the player
                    if(player.getDetectCollision().exactCenterX() >= enemies[i].getDetectCollision().exactCenterX()){
                        //increment countMisses
                        countMisses++;

                        //setting the flag false so that the else part is executed only when new enemy enters the screen
                        flag = false;
                        //if no of Misses is equal to 3, then game is over.
                        if(countMisses==gameOverMissCount){
                            //setting playing false to stop the game.
                            playing = false;
                            isGameOver = true;

                            //stopping the gameon music
                            gameOnsound.stop();
                            //play the game over sound
                            gameOversound.start();
                        }
                    }
                }
            }
        }

        //updating the friend coordinate with respect to player speed
        for(int i=0; i<friendCount; i++){
            friends[i].update(player.getSpeed());

            if(Rect.intersects(player.getDetectCollision(),friends[i].getDetectCollision())){

                //displaying the boom at the collision
                blast.setX(friends[i].getX());
                blast.setY(friends[i].getY());
                //setting playing false to stop the game
                playing = false;
                //setting the isGameOver true as the game is over
                isGameOver = true;

                //stopping the gameon music
                gameOnsound.stop();
                //play the game over sound
                gameOversound.start();
            }
        }
    }


    private void draw() {
        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {

            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.BLACK);

            //setting the paint color to white to draw the stars
            paint.setColor(Color.WHITE);

            //drawing all stars
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            //drawing the score on the game screen
            paint.setTextSize(30);
            canvas.drawText("YO \\m/ Score : "+score,100,50,paint);

            //Drawing the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);


            //drawing the enemies
            for (int i = 0; i < enemyCount; i++) {
                canvas.drawBitmap(
                        enemies[i].getBitmap(),
                        enemies[i].getX(),
                        enemies[i].getY(),
                        paint
                );
            }

            //drawing boom image
            canvas.drawBitmap(
                    blast.getBitmap(),
                    blast.getX(),
                    blast.getY(),
                    paint
            );

            //drawing the friends
            for (int i = 0; i < friendCount; i++) {
                canvas.drawBitmap(
                        friends[i].getBitmap(),
                        friends[i].getX(),
                        friends[i].getY(),
                        paint
                );
            }

            //draw game Over when the game is over
            if(isGameOver){
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos=(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Game Over",canvas.getWidth()/2,yPos,paint);
            }

            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        //when the game is paused
        //setting the variable to false
        playing = false;
        try {
            //stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        //when the game is resumed
        //starting the thread again
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                //stopping the boosting when screen is released
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                //boosting the space jet when screen is pressed
                player.setBoosting();
                break;
        }

        //if the game's over, tappin on game Over screen sends you to MainActivity
        if(isGameOver){
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                context.startActivity(new Intent(context,MainActivity.class));
            }
        }
        return true;
    }

    //stop the music on exit
    public static void stopMusic(){
        gameOnsound.stop();
    }

}

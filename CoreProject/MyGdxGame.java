package net.overmy.mygpgstutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;


public class MyGdxGame extends ApplicationAdapter {

    final AdMob ad;
    final GPGS  gpgs;

    SpriteBatch batch;
    Texture     img;

    float width;
    float height;

    public MyGdxGame( AdMob ad, GPGS gpgs ) {
        this.ad = ad;
        this.gpgs = gpgs;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture( "badlogic.jpg" );

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        Gdx.input.setCatchBackKey( true );
        ad.show();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor( 1, 0, 0, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
        batch.begin();
        batch.draw( img, 0, 0 );
        batch.end();

        boolean escapePressed = Gdx.input.isKeyPressed( Input.Keys.ESCAPE );
        boolean backPressed   = Gdx.input.isKeyPressed( Input.Keys.BACK );

        if ( escapePressed || backPressed ) {
            Gdx.app.exit();
        }

        if ( Gdx.input.justTouched() ) {
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();

            if ( x < width * 0.3f && y < height * 0.5f ) {
                if ( !gpgs.isLoggedIn() ) {
                    gpgs.logIn();
                }
                else {
                    gpgs.logOut();
                }
            }

            if ( x > width * 0.3f && x < width * 0.7f && y < height * 0.5f ) {
                if ( gpgs.isLoggedIn() ) { gpgs.showAchievements(); }
            }

            if ( x > width * 0.7f && y < height * 0.5f ) {
                if ( gpgs.isLoggedIn() ) { gpgs.showLeaderboard(); }
            }

            if ( x < width * 0.3f && y > height * 0.5f ) {
                if ( gpgs.isLoggedIn() ) { gpgs.unlockAchievement( 0 ); }
            }

            if ( x > width * 0.3f && x < width * 0.7f && y > height * 0.5f ) {
                if ( gpgs.isLoggedIn() ) { gpgs.unlockIncrementAchievement( 1, 1 ); }
            }

            if ( x > width * 0.7f && y > height * 0.5f ) {
                if ( gpgs.isLoggedIn() ) {
                    int rndValue = MathUtils.random( 1, 40 );
                    gpgs.submitScore( rndValue );
                    Gdx.app.debug( "Random = ", "" + Integer.toString( rndValue ) );
                }
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}

package net.overmy.labyr1nth.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.overmy.labyr1nth.MyGdxGame;


public class DesktopLauncher {

    static final String     appName      = DesktopLauncher.class.getPackage().getName();
    // change this
    static       SCREEN_CFG screenConfig = SCREEN_CFG.DEFAULT;

    public static void main( String[] arg ) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = screenConfig.getWidth();
        config.height = screenConfig.getHeight();
        config.title = "" + appName + " [ " + config.width + " x " + config.height + " ]";

        new LwjglApplication( new MyGdxGame( new AdMobImpl(), new GPGSImpl() ), config );
    }

    enum SCREEN_CFG {
        GALAXY_TAB2( 1024, 554 ),// not 600
        SQUARE( 1024, 768 ),
        FULL_HD( 1920, 1080 ),
        SMALL( 320, 240 ),
        SCREEN_SHOT( 1024, 500 ),
        DEFAULT( 800, 480 );

        final boolean LANDSCAPE         = true;
        final boolean PORTRAIT          = false;
        final boolean screenOrientation = LANDSCAPE;

        private final int width;
        private final int height;

        private SCREEN_CFG( final int width, final int height ) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return screenOrientation ? width : height;
        }

        public int getHeight() {
            return screenOrientation ? height : width;
        }
    }
}

package net.overmy.mygpgstutorial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import net.overmy.mygpgstutorial.MyGdxGame;

public class AndroidLauncher extends AndroidApplication {

	final AndroidLauncher context = this;

	final AdMobImpl adMob;
	final GPGSImpl gpgs;
	final MyGdxGame game;

	public AndroidLauncher(){
		adMob = new AdMobImpl("Replace ID FROM Google AdMob Console");
		gpgs = new GPGSImpl();
		game = new MyGdxGame( adMob, gpgs );
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );

		adMob.init( context );
		gpgs.init( context );

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useWakelock = true;
		View gameView = initializeForView( game, config );

		RelativeLayout layout = new RelativeLayout( this );
		layout.addView( gameView );
		layout.addView( adMob.adView, adMob.adParams );

		setContentView( layout );
	}

    @Override
    public void onStart() {
        super.onStart();
        // Во время старта приложения, подключаемся к GPGS
        // Так рекомендует делать GOOGLE
        gpgs.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        gpgs.disconnect();
    }

    @Override
    public void onActivityResult( int request, int response, Intent intent ) {
        super.onActivityResult( request, response, intent );
        gpgs.onActivityResult( request, response, intent );
    }
}

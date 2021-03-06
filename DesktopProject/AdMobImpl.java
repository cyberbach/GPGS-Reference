package net.overmy.labyr1nth.desktop;

import com.badlogic.gdx.Gdx;

import net.overmy.labyr1nth.AdMob;


/**
 * Created by Andrey (cb) Mikheev
 * GPGS
 * 26.09.2016
 */
public class AdMobImpl implements AdMob {

    private final String className = AdMob.class.getSimpleName();

    @Override
    public void show() { Gdx.app.debug( className, "show AD" ); }

    @Override
    public void hide() { Gdx.app.debug( className, "hide AD" ); }
}

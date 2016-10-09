package net.overmy.labyr1nth;

import java.util.List;


/**
 * Created by Andrey (cb) Mikheev
 * GPGS
 * 26.09.2016
 */
public interface GPGS {
    public void connect();
    public void disconnect();
    public boolean isConnected();
    public void signOut();

    public void unlockAchievement( int n );
    public void unlockIncrementAchievement( int n, int count );
    public void showAchievements();

    public void submitScore( long score );
    public void showLeaderboard();
}

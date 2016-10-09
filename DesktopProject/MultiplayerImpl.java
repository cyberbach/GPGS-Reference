package net.overmy.gpgstutorial.desktop;

import net.overmy.gpgstutorial.Multiplayer;

import java.util.List;


/**
 * Created by Andrey (cb) Mikheev
 * TutorialGPGS
 * 26.09.2016
 */
public class MultiplayerImpl implements Multiplayer {

    @Override
    public void broadcastMessage( String msg ) {

    }

    @Override
    public List< String > getJoinedParticipants() {
        return null;
    }

    @Override
    public String getMyId() {
        return null;
    }

    @Override
    public String getMyName() {
        return null;
    }

    @Override
    public List< String > getMessageBuffer( char screenTag ) {
        return null;
    }

    @Override
    public void clearMessageBufferExcept( char screenTag ) {

    }

    @Override
    public String getHostId() {
        return null;
    }

    @Override
    public void sendInvitations() {

    }

    @Override
    public void showInvitations() {

    }

    @Override
    public void startQuickGame() {

    }

    @Override
    public void leaveGame() {

    }

    @Override
    public void rematch() {

    }

    @Override
    public void setNoOfPlayers( int noOfPlayers ) {

    }

    @Override
    public int getNoOfPlayers() {
        return 0;
    }

    @Override
    public void showSendIntent() {

    }
}

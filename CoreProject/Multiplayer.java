package net.overmy.gpgstutorial;

import java.util.List;


/**
 * Created by Andrey (cb) Mikheev
 * GPGS
 * 26.09.2016
 */
public interface Multiplayer {
    public void broadcastMessage(String msg);
    public List<String> getJoinedParticipants();
    public String getMyId();
    public String getMyName();
    public List<String> getMessageBuffer(char screenTag);
    public void clearMessageBufferExcept(char screenTag);
    public String getHostId();
    public void sendInvitations();
    public void showInvitations();
    //for main menu usages
    public void startQuickGame();
    public void leaveGame();
    public void rematch();
    public void setNoOfPlayers(int noOfPlayers);
    public int getNoOfPlayers();
    public void showSendIntent();
}

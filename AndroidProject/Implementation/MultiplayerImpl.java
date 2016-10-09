package net.overmy.gpgstutorial.GooglePlayGameServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.games.request.GameRequest;

import net.overmy.gpgstutorial.AndroidLauncher;
import net.overmy.gpgstutorial.Multiplayer;
import net.overmy.gpgstutorial.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Andrey (cb) Mikheev
 * TutorialGPGS
 * 26.09.2016
 */
public class MultiplayerImpl implements Multiplayer, RealTimeMessageReceivedListener,
                                        RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener {

    final static         String className           = MultiplayerImpl.class.getSimpleName();
    final static         int    RC_SELECT_PLAYERS   = 10000;
    final static         int    RC_INVITATION_INBOX = 10001;
    final static         int    RC_WAITING_ROOM     = 10002;
    private static final int    DEFAULT_LIFETIME    = 7;
    private static final int    SEND_GIFT_CODE      = 2;
    private static final int    SEND_REQUEST_CODE   = 3;
    protected GoogleApiClient client;
    protected AndroidLauncher context;
    String                   mRoomId       = null;
    ArrayList< Participant > mParticipants = null;
    String                   mMyId         = null;
    String mMyName;
    String mIncomingInvitationId = null;
    private ArrayList< String > participants = new ArrayList<>();
    private int                 noOfPlayers  = 1;
    private MessageBuffer       msgBuf       = new MessageBuffer();
    private   String          hostId;

    public void init( AndroidLauncher context, GoogleApiClient client ) {
        this.context = context;
        this.client = client;
        mGiftIcon = BitmapFactory.decodeResource( context.getResources(), R.drawable.ic_launcher );
    }

    public void onActivityResult( int request, int response, Intent intent ) {
        Log.d( className, "requestCode: " + request );
        switch ( request ) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                handleSelectPlayersResult( response, intent );
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult( response, intent );
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if ( response == Activity.RESULT_OK ) {
                    // ready to start playing
                    Log.d( className, "Starting game (waiting room returned OK)." );
                    startGame();
                }
                else {
                    if ( response == GamesActivityResultCodes.RESULT_LEFT_ROOM ) {
                        // player indicated that they want to leave the room
                        leaveRoom();
                    }
                    else {
                        if ( response == Activity.RESULT_CANCELED ) {
                            // Dialog was cancelled (user pressed back key, for instance). In our game,
                            // this means leaving the room too. In more elaborate games, this could mean
                            // something else (like minimizing the waiting room UI).
                            leaveRoom();
                        }
                    }
                }
                break;
        }
    }

    public void startQuickGame() {
        context.keepScreenOff();

        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder( this );
        roomConfigBuilder.setMessageReceivedListener( this );
        roomConfigBuilder.setRoomStatusUpdateListener( this );

        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = noOfPlayers;
        Bundle criteria = RoomConfig.createAutoMatchCriteria( MIN_OPPONENTS, MAX_OPPONENTS, 0 );
        roomConfigBuilder.setAutoMatchCriteria( criteria );

        Games.RealTimeMultiplayer.create( client, roomConfigBuilder.build() );
        context.keepScreenOn();
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult( int response, Intent data ) {
        if ( response != Activity.RESULT_OK ) {
            Log.w( className, "*** select players UI cancelled, " + response );
            return;
        }

        Log.d( className, "Select players UI succeeded." );

        // get the invitee list
        final ArrayList< String > invitees = data.getStringArrayListExtra( Games.EXTRA_PLAYER_IDS );
        Log.d( className, "Invitee count: " + invitees.size() );

        // get the automatch criteria
        Bundle autoMatchCriteria   = null;
        int    minAutoMatchPlayers = 1;//data.getIntExtra( MultiplayerImpl.EXTRA_MIN_AUTOMATCH_PLAYERS, 0 );
        int    maxAutoMatchPlayers = noOfPlayers;//data.getIntExtra( MultiplayerImpl.EXTRA_MAX_AUTOMATCH_PLAYERS, 0 );
        if ( minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0 ) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0 );
            Log.d( className, "Automatch criteria: " + autoMatchCriteria );
        }

        // create the room
        Log.d( className, "Creating room..." );
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder( this );
        rtmConfigBuilder.addPlayersToInvite( invitees );
        rtmConfigBuilder.setMessageReceivedListener( this );
        rtmConfigBuilder.setRoomStatusUpdateListener( this );
        if ( autoMatchCriteria != null ) {
            rtmConfigBuilder.setAutoMatchCriteria( autoMatchCriteria );
        }
        //game.setMainMenuScreen(TiledMapGame.SCREEN_LOADING);
        //        switchToScreen(R.id.screen_wait);
        context.keepScreenOn();
        Games.RealTimeMultiplayer.create( client, rtmConfigBuilder.build() );
        Log.d( className, "Room created, waiting for it to be ready..." );
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult( int response, Intent data ) {
        if ( response != Activity.RESULT_OK ) {
            Log.w( className, "*** invitation inbox UI cancelled, " + response );
            return;
        }

        Log.d( className, "Invitation inbox UI succeeded." );
        Invitation inv = data.getExtras().getParcelable(
                com.google.android.gms.games.multiplayer.Multiplayer.EXTRA_INVITATION );

        // accept invitation
        acceptInviteToRoom( inv.getInvitationId() );
    }

    // Accept the given invitation.
    void acceptInviteToRoom( String invId ) {
        // accept the invitation
        Log.d( className, "Accepting invitation: " + invId );
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder( this );
        roomConfigBuilder.setInvitationIdToAccept( invId )
                         .setMessageReceivedListener( this )
                         .setRoomStatusUpdateListener( this );
        //game.setMainMenuScreen(TiledMapGame.SCREEN_LOADING);
        //        switchToScreen(R.id.screen_wait);
        context.keepScreenOn();
        Games.RealTimeMultiplayer.join( client, roomConfigBuilder.build() );
    }

    @Override
    public void leaveGame() {
        // game.leaveGame();
        leaveRoom();
    }

    // Leave the room.
    void leaveRoom() {
        Log.d( className, "Leaving room." );
        context.keepScreenOff();
        if ( mRoomId != null ) {
            Games.RealTimeMultiplayer.leave( client, this, mRoomId );
            mRoomId = null;
            //            switchToScreen(R.id.screen_wait);
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom( Room room ) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        //final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent( client,
                                                                   room, noOfPlayers );

        // show waiting room UI
        context.startActivityForResult( i, RC_WAITING_ROOM );
    }

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived( final Invitation invitation ) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        //Gdx.app.log("INVITATION", "Received an invitation!");
        mIncomingInvitationId = invitation.getInvitationId();
        // accept invitation
        AlertDialog.Builder builder = new AlertDialog.Builder( context,
                                                               AlertDialog.THEME_HOLO_LIGHT );
        builder.setTitle( "Game Invitation" )
               .setMessage( invitation.getInviter().getDisplayName() + ": COME FIGHT ME NOOB!!" )
               .setPositiveButton( "Accept", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick( DialogInterface dialog, int which ) {
                       acceptInviteToRoom( invitation.getInvitationId() );
                   }
               } )
               .setNegativeButton( "Decline", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick( DialogInterface dialog, int which ) {
                       // cancel dialog
                   }
               } );
        //        builder.create();
        builder.show();

        //        switchToScreen(mCurScreen); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved( String invitationId ) {
        if ( mIncomingInvitationId.equals( invitationId ) ) {
            mIncomingInvitationId = null;
        }
    }

    // For CharacterSelector class
    @Override
    public String getMyName() {
        return mMyName;
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override

    public void onConnectedToRoom( Room room ) {
        Log.d( className, "onConnectedToRoom." );

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId( Games.Players.getCurrentPlayerId( client ) );
        mMyName = Games.Players.getCurrentPlayer( client ).getDisplayName();
        // print out the list of participants (for debug purposes)
        Log.d( className, "Room ID: " + mRoomId );
        Log.d( className, "My ID " + mMyId );
        Log.d( className, "My Name " + mMyName );
        Log.d( className, "<< CONNECTED TO ROOM>>" );
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom( int statusCode, String roomId ) {
        // we have left the room; return to main screen.
        Log.d( className, "onLeftRoom, code " + statusCode );
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom( Room room ) {
        mRoomId = null;
        showGameError();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated( int statusCode, Room room ) {
        Log.d( className, "onRoomCreated(" + statusCode + ", " + room + ")" );
        if ( statusCode != GamesStatusCodes.STATUS_OK ) {
            Log.e( className, "*** Error: onRoomCreated, status " +
                              GamesStatusCodes.getStatusString( statusCode ) );
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom( room );
    }


    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        String error = "Комната закрыта";
        BaseGameUtils.makeSimpleDialog( context, error );
    }


    // Called when room is fully connected.
    @Override
    public void onRoomConnected( int statusCode, Room room ) {
        Log.d( className, "onRoomConnected(" + statusCode + ", " + room + ")" );
        if ( statusCode != GamesStatusCodes.STATUS_OK ) {
            Log.e( className, "*** Error: onRoomConnected, status " + statusCode );
            showGameError();
            return;
        }
        updateRoom( room );
    }

    @Override
    public void onJoinedRoom( int statusCode, Room room ) {
        Log.d( className, "onJoinedRoom(" + statusCode + ", " + room + ")" );
        if ( statusCode != GamesStatusCodes.STATUS_OK ) {
            Log.e( className, "*** Error: onRoomConnected, status " + statusCode );
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom( room );
    }

    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined( Room room, List< String > arg1 ) {
        updateRoom( room );
    }

    @Override
    public void onPeerInvitedToRoom( Room room, List< String > arg1 ) {
        updateRoom( room );
    }

    @Override
    public void onP2PDisconnected( String participant ) {
    }

    @Override
    public void onP2PConnected( String participant ) {
    }

    @Override
    public void onPeerJoined( Room room, List< String > arg1 ) {
        updateRoom( room );
    }


    @Override
    public void onPeerLeft( Room room, List< String > peersWhoLeft ) {
        updateRoom( room );
    }

    @Override
    public void onRoomAutoMatching( Room room ) {
        updateRoom( room );
    }

    @Override
    public void onRoomConnecting( Room room ) {
        updateRoom( room );
    }

    @Override
    public void onPeersConnected( Room room, List< String > peers ) {
        updateRoom( room );
    }

    @Override
    public void onPeersDisconnected( Room room, List< String > peers ) {
        updateRoom( room );
    }

    void updateRoom( Room room ) {
        if ( room != null ) {
            mParticipants = room.getParticipants();
        }
        if ( mParticipants != null ) {
            //   updatePeerScoresDisplay();
        }
    }

    // Start the gameplay phase of the game.
    void startGame() {
        List< String > ids = getJoinedParticipants();
        Collections.sort( ids );
        hostId = ids.get( 0 );
        Log.d( "Host", hostId );
        // TODO game.startGame();
        //        switchToScreen(R.id.screen_game);
    }

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived( RealTimeMessage rtm ) {
        byte[] buf = rtm.getMessageData();
        String msg = new String( buf );
        if ( msg.startsWith( className ) ) {
            //parse message
            String msgStr = rtm.getSenderParticipantId() + "," +
                            msg.substring( className.length(), msg.length() - 1 );
            msgBuf.add( msgStr, msg.charAt( msg.length() - 1 ) );
        }
        //        Log.d("Receiving", msg);
    }

    @Override
    public void sendInvitations() {
        context.keepScreenOff();
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent( client, 1, 3 );
        context.startActivityForResult( intent, RC_SELECT_PLAYERS );
        context.keepScreenOn();
    }

    @Override
    public void showInvitations() {
        Intent intent = Games.Invitations.getInvitationInboxIntent( client );
        context.startActivityForResult( intent, RC_INVITATION_INBOX );
    }

    @Override
    public void broadcastMessage( String msg ) {
        //        Log.d("Sending", msg);
        String taggedMsg = className + msg;// + game.getScreenTag();

        byte[] bytes = taggedMsg.getBytes();

        int peers = 0;
        for ( Participant p : mParticipants ) {
            if ( p.getParticipantId().equals( mMyId ) ) { continue; }
            if ( p.getStatus() != Participant.STATUS_JOINED ) { continue; }
            if ( mRoomId != null ) {
                peers += 1;
                Games.RealTimeMultiplayer.sendUnreliableMessage( client, bytes, mRoomId,
                                                                 p.getParticipantId() );
            }
        }
        if ( peers + 1 < mParticipants.size() ) {
            //if (game.isInGame()) {
            //    game.leaveGame();
            //}
        }
    }

    @Override
    public List< String > getJoinedParticipants() {
        participants.clear();
        for ( Participant p : mParticipants ) {
            if ( p.getStatus() != Participant.STATUS_JOINED ) { continue; }
            participants.add( p.getParticipantId() );
        }
        return participants;
    }

    @Override
    public String getMyId() {
        return mMyId;
    }

    @Override
    public List< String > getMessageBuffer( char screenTag ) {
        return msgBuf.getList( screenTag );
    }

    @Override
    public void clearMessageBufferExcept( char screenTag ) {
        msgBuf.clearMessageBufferExcept( screenTag );
    }

    @Override
    public String getHostId() {
        return hostId;
    }

    @Override
    public void rematch() {
        context.keepScreenOff();
        if ( this.getJoinedParticipants().size() > 1 ) {
            startGame();
        }
        context.keepScreenOn();
    }
    Bitmap mGiftIcon;

    @Override
    public void showSendIntent() {
        Intent intent = Games.Requests.getSendIntent( client, GameRequest.TYPE_WISH,
                                                      "".getBytes(), DEFAULT_LIFETIME, mGiftIcon,
                                                      "get you gift bitch" );
        context.startActivityForResult( intent, SEND_REQUEST_CODE );
    }

    @Override
    public int getNoOfPlayers() {
        return noOfPlayers;
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    @Override
    public void setNoOfPlayers( int noOfPlayers ) {
        this.noOfPlayers = noOfPlayers;
    }
}


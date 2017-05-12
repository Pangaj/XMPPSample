package shruthi.pangaj.chatprotocol.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.File;
import java.io.IOException;

import shruthi.pangaj.chatprotocol.Constants;

/**
 * Created by Pangaj on 31/03/17.
 */
public class RoosterConnection implements ConnectionListener {
    private static final String TAG = "RoosterConnection";

    private final Context mApplicationContext;
    private final String mUsername;
    private final String mPassword;
    private final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.
    private ChatMessageListener messageListener;
    public static XMPPConnection connection;
    private String contactJid;

    public static enum ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED;
    }

    public static enum LoggedInState {
        LOGGED_IN, LOGGED_OUT;
    }

    public RoosterConnection(Context context) {
        Log.d(TAG, "RoosterConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid", null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password", null);

        if (jid != null) {
            mUsername = jid.split("@")[0];
            mServiceName = jid.split("@")[1];
        } else {
            mUsername = "";
            mServiceName = "";
        }
    }


    public void connect() throws IOException, XMPPException, SmackException {
        Log.d(TAG, "Connecting to server " + mServiceName);
        XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder();
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.allowEmptyOrNullUsernames();
        builder.setResource("Rooster");

        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();

        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        mConnection.setPacketReplyTimeout(100000);
        SASLAuthentication.unBlacklistSASLMechanism("SCRAM-SHA-1");
        mConnection.connect();


        DeliveryReceiptManager dm = DeliveryReceiptManager.getInstanceFor(mConnection);
        dm.addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                Log.d(TAG, "RECEIPT_RECEIVED_PAN");
            }
        });
//        connected = true;
//    return isconnecting = false;
        mConnection.login();

        messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                ///ADDED
                Log.d(TAG, "message.getBody() :" + message.getBody());
                Log.d(TAG, "message.getFrom() :" + message.getFrom());

                String from = message.getFrom();
                String contactJid = "";
                if (from.contains("/")) {
                    contactJid = from.split("/")[0];
                    Log.d(TAG, "The real jid is :" + contactJid);
                } else {
                    contactJid = from;
                }

                Packet received = new Message();
                received.addExtension(new DeliveryReceipt(message.getPacketID()));
                received.setTo(message.getFrom());

                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                try {
                    mConnection.sendPacket((Stanza) received);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");

                //Bundle up the intent and send the broadcast.
                Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, contactJid);
                intent.putExtra(RoosterConnectionService.BUNDLE_TYPE, Constants.TEXT);
                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                mApplicationContext.sendBroadcast(intent);
                Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                ///ADDED
            }
        };

        // Add a packet listener to get messages sent to us
        mConnection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Message message = (Message) packet;
                if (message.getBody() != null) {
                    Log.d(TAG, message.getFrom());
                    Log.d(TAG, message.getBody());
                    // Add incoming message to the list view or similar

                   /* From Single Chat */
                  /*  String from = message.getFrom();
                    String contactJid = "";
                    if (from.contains("/")) {
                        contactJid = from.split("/")[0];
                        Log.d(TAG, "The real jid is :" + contactJid);
                    } else {
                        contactJid = from;
                    }

                    Packet received = new Message();
                    received.addExtension(new DeliveryReceipt(message.getPacketID()));
                    received.setTo(message.getFrom());

                    Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                    try {
                        mConnection.sendPacket(received);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");

                    //Bundle up the intent and send the broadcast.
                    Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, contactJid);
                    intent.putExtra(RoosterConnectionService.BUNDLE_TYPE, Constants.TEXT);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                    mApplicationContext.sendBroadcast(intent);
                    Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");*/
                    /* From Single Chat */

                }
            }
        }, MessageTypeFilter.GROUPCHAT);

        //The snippet below is necessary for the message listener to be attached to our connection.
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {

                //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                chat.addMessageListener(messageListener);

            }
        });

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

    }

    private void setupUiThreadBroadCastMessageReceiver() {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if (action.equals(RoosterConnectionService.SEND_MESSAGE)) {
                    //Send the message.
                    sendMessage(intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(RoosterConnectionService.BUNDLE_TO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver, filter);

    }

    private void sendMessage(String body, String toJid) {
        Log.d(TAG, "Sending message to :" + toJid);
        Chat chat = ChatManager.getInstanceFor(mConnection).createChat(toJid, messageListener);
        try {
            Message message = new Message();
            message.setBody(body);

            DeliveryReceiptManager.addDeliveryReceiptRequest(message);

            chat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    public void disconnect() {
        Log.d(TAG, "Disconnecting from server " + mServiceName);
        if (mConnection != null) {
            mConnection.disconnect();
        }

        mConnection = null;
        // Unregister the message broadcast receiver.
        if (uiThreadMessageReceiver != null) {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }
    }


    @Override
    public void connected(XMPPConnection connection) {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Connected Successfully");

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "Authenticated Successfully");
        showContactListActivityWhenAuthenticated();
        this.connection = connection;
        getImage();
    }

    public static XMPPConnection getConnection() {
        return connection;
    }

    @Override
    public void connectionClosed() {
        RoosterConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "Connectionclosed()");

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        RoosterConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ConnectionClosedOnError, error " + e.toString());

    }

    @Override
    public void reconnectingIn(int seconds) {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTING;
        Log.d(TAG, "ReconnectingIn() ");

    }

    @Override
    public void reconnectionSuccessful() {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG, "ReconnectionSuccessful()");

    }

    @Override
    public void reconnectionFailed(Exception e) {
        RoosterConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG, "ReconnectionFailed()");

    }

    private void showContactListActivityWhenAuthenticated() {
        Intent i = new Intent(RoosterConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG, "Sent the broadcast that we are authenticated");
    }

    private void getImage() {
        FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
        manager.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(final FileTransferRequest request) {
                new Thread() {
                    @Override
                    public void run() {
                        IncomingFileTransfer transfer = request.accept();
                        Log.d(TAG, request.getRequestor());
                        System.out.println("Name: " + request.getFileName());
                        System.out.println("Description : " + request.getDescription());
                        System.out.println("Size: " + request.getFileSize());
                        System.out.println("Mime: " + request.getMimeType());

                        System.out.println("File Size : " + transfer.getFileSize());
                        System.out.println("Peer : " + transfer.getPeer());
                        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "LZoom" + File.separator + transfer.getFileName());
                        try {
                            transfer.recieveFile(f);
                            while (!transfer.isDone()) {
                                try {
                                    Thread.sleep(1000L);
                                } catch (Exception e) {
                                    Log.e("", e.getMessage());
                                }
                                if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                    Log.e("ERROR!!! ", transfer.getError() + "");
                                }
                                if (transfer.getException() != null) {
                                    transfer.getException().printStackTrace();
                                }
                            }
                            if (transfer.isDone()) {
                                Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                                intent.setPackage(mApplicationContext.getPackageName());
                                intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, request.getRequestor());
                                intent.putExtra(RoosterConnectionService.BUNDLE_TYPE, Constants.IMAGE);
                                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, Environment.getExternalStorageDirectory() + File.separator + "LZoom" + File.separator + transfer.getFileName());
                                mApplicationContext.sendBroadcast(intent);
                            }
                        } catch (
                                Exception e) {
                            Log.e("", e.getMessage());
                        }
                    }
                }.start();
            }
        });
    }
}

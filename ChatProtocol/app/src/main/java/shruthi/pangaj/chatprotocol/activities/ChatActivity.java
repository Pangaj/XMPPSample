package shruthi.pangaj.chatprotocol.activities;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import shruthi.pangaj.chatprotocol.Constants;
import shruthi.pangaj.chatprotocol.R;
import shruthi.pangaj.chatprotocol.adapters.ChatAdapter;
import shruthi.pangaj.chatprotocol.model.MessageModel;
import shruthi.pangaj.chatprotocol.rooster.RoosterConnection;
import shruthi.pangaj.chatprotocol.rooster.RoosterConnectionService;

/**
 * Created by Pangaj on 31/03/17.
 */

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private String contactJid;
    private String contactJidWithResource;
    //    private ChatView mChatView;
//    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver;
    private RecyclerView rvChatLayout;
    private EditText etMessage;
    private ImageView ivSend;
    private ArrayList<MessageModel> messageModel;
    private ChatAdapter adapter;
    private LinearLayoutManager layoutManager;
    private View tvDots;
    private LinearLayout llAttach;
    private ImageView ivAudio;
    private ImageView ivCamera;
    private ImageView ivAttach;
    private MultiUserChat mMultiUserChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChatLayout = (RecyclerView) findViewById(R.id.rv_chat_list);
        etMessage = (EditText) findViewById(R.id.et_text_message);
        ivSend = (ImageView) findViewById(R.id.iv_send);
        tvDots = findViewById(R.id.tv_dots);
        llAttach = (LinearLayout) findViewById(R.id.ll_text_missing);
        ivCamera = (ImageView) findViewById(R.id.iv_camera);
        ivAudio = (ImageView) findViewById(R.id.iv_audio);
        ivAttach = (ImageView) findViewById(R.id.iv_attach);

        messageModel = new ArrayList<>();

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Only send the message if the client is connected
                //to the server.
                if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sendint Message");
                    //Send the message to the server

                    if (contactJid.equals("channelgroup@conference.lzoom.com")) {
                        try {
                            Message msg = new Message("channelgroup@conference.lzoom.com", Message.Type.groupchat);
                            msg.setBody(etMessage.getText().toString());
                            mMultiUserChat.sendMessage(msg);
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
                        intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, etMessage.getText().toString());
                        intent.putExtra(RoosterConnectionService.BUNDLE_TO, contactJid);
                        sendBroadcast(intent);
                    }

                    //Update the chat view.
                    addMessage(new MessageModel(etMessage.getText().toString().trim(), null, System.currentTimeMillis(), Constants.SENT, Constants.TEXT, Constants.PROGRESS_NONE));
                    etMessage.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Client not connected to server ,Message not sent!", Toast.LENGTH_LONG).show();
                }
            }
        });
        Intent intent = getIntent();
        contactJid = intent.getStringExtra("EXTRA_CONTACT_JID");
        contactJidWithResource = intent.getStringExtra("EXTRA_CONTACT_JID_WITH_RESOURCE");
        setTitle(contactJid);

        if (contactJid.equals("channelgroup@conference.lzoom.com")) {
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(RoosterConnection.getConnection());
            mMultiUserChat = manager.getMultiUserChat("channelgroup@conference.lzoom.com");
            try {
//                            mMultiUserChat.create("Jaya");
                if (!mMultiUserChat.isJoined()) {
                    mMultiUserChat.join("Jaya");
                }
            } catch (SmackException | XMPPException e) {
                e.printStackTrace();
            }
        }

        adapter = new ChatAdapter(ChatActivity.this, messageModel);
        rvChatLayout.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        rvChatLayout.setLayoutManager(layoutManager);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (etMessage.length() > 0) {
                    llAttach.setVisibility(View.GONE);
                    ivSend.setVisibility(View.VISIBLE);
                } else {
                    llAttach.setVisibility(View.VISIBLE);
                    ivSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        ivAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        ivAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), RecordAudioActivity.class), 12);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.NEW_MESSAGE:
                        String from = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                        String body = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                        if (from.equals(contactJid) || from.equals(contactJidWithResource)) {
                            if (body == null) {
/*                                tvDots.setVisibility(View.VISIBLE);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvDots.setVisibility(View.GONE);
                                    }
                                }, 1200);*/
                            } else {
                                int messageType = intent.getIntExtra(RoosterConnectionService.BUNDLE_TYPE, 0);
                                if (messageType == (Constants.TEXT)) {
                                    addMessage(new MessageModel(body, null, System.currentTimeMillis(), Constants.RECIEVED, Constants.TEXT, Constants.PROGRESS_NONE));
                                } else if (messageType == (Constants.IMAGE)) {
                                    addMessage(new MessageModel(body, body, System.currentTimeMillis(), Constants.RECIEVED, Constants.IMAGE, Constants.PROGRESS_NONE));
                                }
                            }
                        } else {
                            Log.d(TAG, "Got a message from jid :" + from);
                        }
                        return;
                }
            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private int addMessage(MessageModel message) {
        messageModel.add(message);
        adapter.swap(messageModel);
        return messageModel.size();
    }

    private void update(ArrayList<MessageModel> message) {
        adapter.swap(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_image) {
            Intent takePic = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(takePic, 10);

        } else if (item.getItemId() == R.id.menu_video) {
            Intent takePic = new Intent(Intent.ACTION_PICK,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(takePic, 14);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && null != data) {
            final int[] position = new int[1];
            new Thread() {
                @Override
                public void run() {
                    FileTransferManager manager = FileTransferManager.getInstanceFor(RoosterConnection.getConnection());
                    final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(contactJidWithResource);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        final String fileName = cursor.getString(columnIndex);
                        cursor.close();
                        final File file = new File(fileName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                position[0] = addMessage(new MessageModel(fileName, fileName, System.currentTimeMillis(), Constants.SENT, Constants.IMAGE, Constants.PROGRESS_ONGOING));
                                position[0]--;
                            }
                        });
                        try {
                            transfer.sendFile(file, "test_file");
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception e) {
                                Log.e("", e.getMessage());
                            }
                            if (transfer.getStatus().equals(FileTransfer.Status.complete)) {
                                Log.d(TAG, "IMAGE -> SUCCESS");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                Log.d(TAG, "IMAGE -> ERROR");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.isDone()) {
                                Log.d(TAG, "IMAGE -> CANCELLED");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }.start();
        } else if (requestCode == 11 && resultCode == RESULT_OK && null != data) {
            final int[] position = new int[1];
            new Thread() {
                @Override
                public void run() {
                    FileTransferManager manager = FileTransferManager.getInstanceFor(RoosterConnection.getConnection());
                    final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(contactJidWithResource);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        final String fileName = cursor.getString(columnIndex);
                        final File file = new File(fileName);
                        cursor.close();
                        createVideoThumbNail(fileName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                position[0] = addMessage(new MessageModel(fileName, Environment.getExternalStorageDirectory() + "/LZoom/seconds.png", System.currentTimeMillis(), Constants.SENT, Constants.VIDEO, Constants.PROGRESS_ONGOING));
                                position[0]--;
                            }
                        });
                        try {
                            transfer.sendFile(file, "test_file");
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception e) {
                                Log.e("", e.getMessage());
                            }
                            if (transfer.getStatus().equals(FileTransfer.Status.complete)) {
                                Log.d(TAG, "VIDEO -> SUCCESS");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                Log.d(TAG, "VIDEO -> ERROR");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.isDone()) {
                                Log.d(TAG, "VIDEO -> CANCELLED");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }.start();
        } else if (requestCode == 12 && resultCode == RESULT_OK && null != data) {
            final int[] position = new int[1];
            new Thread() {
                @Override
                public void run() {
                    FileTransferManager manager = FileTransferManager.getInstanceFor(RoosterConnection.getConnection());
                    final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(contactJidWithResource);
                    final String fileName = data.getData().toString();
                    if (fileName != null) {
                        final File file = new File(fileName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                position[0] = addMessage(new MessageModel(fileName, null, System.currentTimeMillis(), Constants.SENT, Constants.AUDIO, Constants.PROGRESS_ONGOING));
                                position[0]--;
                            }
                        });
                        try {
                            transfer.sendFile(file, "test_file");
                        } catch (SmackException e) {
                            e.printStackTrace();
                        }
                        while (true) {
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception e) {
                                Log.e("", e.getMessage());
                            }
                            if (transfer.getStatus().equals(FileTransfer.Status.complete)) {
                                Log.d(TAG, "AUDIO -> SUCCESS");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                                Log.d(TAG, "AUDIO -> ERROR");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            } else if (transfer.isDone()) {
                                Log.d(TAG, "AUDIO -> CANCELLED");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageModel.get(position[0]).setProgressStatus(Constants.PROGRESS_NONE);
                                        update(messageModel);
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }.start();
        } else if (requestCode == 13 && resultCode == RESULT_OK && null != data) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri imageUri = getImageUri(photo);
            getImageData(new File(getRealPathFromURI(imageUri)), imageUri);
        } else if (requestCode == 14 && resultCode == RESULT_OK && null != data) {
            getVideoUrl(data.getData());
        }
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getVideoUrl(Uri videoUri) {
        FileInputStream objFileIS = null;
        try {
            objFileIS = new FileInputStream(new File(getRealPathFromURI(videoUri)));
            Log.d(TAG, "getVideoUri: " + videoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream objByteArrayOS = new ByteArrayOutputStream();
        byte[] byteBufferString = new byte[1024];
        try {
            for (int readNum; (readNum = objFileIS.read(byteBufferString)) != -1; ) {
                objByteArrayOS.write(byteBufferString, 0, readNum);
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String videoData = Base64.encodeToString(objByteArrayOS.toByteArray(), Base64.DEFAULT);
        Log.d("getVideoUrl", videoData);
        return videoData;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentURI, projection, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public String getImageData(File Filepath, Uri imageUri) {
        String encodedImage = null;
        if (Filepath != null) {
            Bitmap fullImage = null;
            try {
                fullImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            fullImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
            Log.d(TAG, "getImageData: " + encodedImage);
        }
        return encodedImage;
    }

    private void createVideoThumbNail(String path) {
        saveBitMap(ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND));
    }

    private void saveBitMap(Bitmap mBitmap) {
        File f3 = new File(Environment.getExternalStorageDirectory() + "/LZoom/");
        f3.mkdirs();
        OutputStream outStream = null;
        File file = new File(f3, "seconds.png");
        try {
            outStream = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.close();
            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, "saveBitMap: " + e.getMessage());
        }
    }

    private Boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public void openCamera() {
        if (checkStoragePermission()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {

                       /* Intent takePictureOrVideo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePictureOrVideo, 13);*/

                        Intent takePictureOrVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        startActivityForResult(takePictureOrVideo, 14);
                    } catch (Exception e) {
//                    showAlertDialog(getResources().getString(R.string.permission_required), LZAlertDialogFragment.TYPE_ERROR);
                    }
                }
            }, 1500);
            Log.e(TAG, "grant");
        }
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("*/*");
        startActivityForResult(photoPickerIntent, 200);
    }

    @Override
    public void onBackPressed() {
        adapter.removeHandlerCallBacks();
        super.onBackPressed();
    }
}

package shruthi.pangaj.chatprotocol.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import shruthi.pangaj.chatprotocol.Constants;
import shruthi.pangaj.chatprotocol.R;
import shruthi.pangaj.chatprotocol.model.MessageModel;

/**
 * Created by Pangaj on 31/03/17.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {
    private String TAG = "ChatAdapter";
    private ArrayList<MessageModel> messageModel;
    private Activity activity;
    private int progress;
    private Handler mHandler = new Handler();

    private AudioManager audioManager = null;

    private MediaPlayer chatAudioPlayer = null;
    private ImageView ivChatAudio;
    private SeekBar sbChatAudio;
    private TextView tvChatAudioTime;
    private String chatTimeStamp;
    private int chatPosition = -1;

    //    UI elements
    private final int TEXTVIEW_SENT = 1;
    private final int TEXTVIEW_RECIEVED = 2;

    private final int IMAGE_SENT = 3;
    private final int IMAGE_RECIEVED = 4;

    private final int VIDEO_SENT = 5;
    private final int VIDEO_RECEIVED = 6;

    private final int AUDIO_SENT = 7;
    private final int AUDIO_RECEIVED = 8;

    public ChatAdapter(Activity activity, ArrayList<MessageModel> messageModel) {
        this.messageModel = messageModel;
        this.activity = activity;
        // Updating progress bar
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TEXTVIEW_SENT:
                View sentText = inflater.inflate(R.layout.chat_text_sent, parent, false);
                viewHolder = new SentTextHolder(sentText);
                break;
            case TEXTVIEW_RECIEVED:
                View reveivedText = inflater.inflate(R.layout.chat_text_rcv, parent, false);
                viewHolder = new ReceivedTextHolder(reveivedText);
                break;
            case IMAGE_SENT:
                View sendImage = inflater.inflate(R.layout.chat_image_sent, parent, false);
                viewHolder = new SentImageHolder(sendImage);
                break;
            case IMAGE_RECIEVED:
                View receivedImage = inflater.inflate(R.layout.chat_image_rcv, parent, false);
                viewHolder = new ReceivedImageHolder(receivedImage);
                break;
            case VIDEO_SENT:
                View sentVideo = inflater.inflate(R.layout.chat_image_sent, parent, false);
                viewHolder = new SentImageHolder(sentVideo);
                break;
            case VIDEO_RECEIVED:
                View receivedVideo = inflater.inflate(R.layout.chat_image_rcv, parent, false);
                viewHolder = new ReceivedImageHolder(receivedVideo);
                break;
            case AUDIO_SENT:
                View sentAudio = inflater.inflate(R.layout.chat_audio_sent, parent, false);
                viewHolder = new SentAudioHolder(sentAudio);
                break;
            case AUDIO_RECEIVED:
                View receivedAudio = inflater.inflate(R.layout.chat_audio_rcv, parent, false);
                viewHolder = new ReceivedAudioHolder(receivedAudio);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case TEXTVIEW_SENT:
                SentTextHolder sentTextHolder = (SentTextHolder) viewHolder;
                configureSentText(sentTextHolder, position);
                break;
            case TEXTVIEW_RECIEVED:
                ReceivedTextHolder receivedTextHolder = (ReceivedTextHolder) viewHolder;
                configureReceivedText(receivedTextHolder, position);
                break;
            case IMAGE_SENT:
                SentImageHolder sentImageHolder = (SentImageHolder) viewHolder;
                configureSentImage(sentImageHolder, position, false);
                break;
            case IMAGE_RECIEVED:
                ReceivedImageHolder receivedImageHolder = (ReceivedImageHolder) viewHolder;
                configureReceivedImage(receivedImageHolder, position, false);
                break;
            case VIDEO_SENT:
                SentImageHolder sentVideoHolder = (SentImageHolder) viewHolder;
                configureSentImage(sentVideoHolder, position, true);
                break;
            case VIDEO_RECEIVED:
                ReceivedImageHolder receivedVideoHolder = (ReceivedImageHolder) viewHolder;
                configureReceivedImage(receivedVideoHolder, position, true);
                break;
            case AUDIO_SENT:
                SentAudioHolder sentAudioHolder = (SentAudioHolder) viewHolder;
                configureSentAudio(sentAudioHolder, position);
                break;
            case AUDIO_RECEIVED:
                ReceivedAudioHolder receivedAudioHolder = (ReceivedAudioHolder) viewHolder;
                configureReceivedAudio(receivedAudioHolder, position);
                break;
        }
    }

    private void configureReceivedText(final ReceivedTextHolder receivedMessageHolder, final int position) {
        receivedMessageHolder.tvMessage.setText(messageModel.get(position).getMessage());
        receivedMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());
    }

    private void configureReceivedImage(final ReceivedImageHolder receivedMessageHolder, final int position, final Boolean isVideo) {
        Bitmap bitmap = BitmapFactory.decodeFile(messageModel.get(position).getThumbNail());
        receivedMessageHolder.ivMessage.setImageBitmap(bitmap);
        if (messageModel.get(position).getProgressStatus() == 1) {
            receivedMessageHolder.pbImage.setVisibility(View.VISIBLE);
        } else {
            receivedMessageHolder.pbImage.setVisibility(View.GONE);
        }
        receivedMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());

        if (isVideo) {
            receivedMessageHolder.ivPlay.setVisibility(View.VISIBLE);
        }

        receivedMessageHolder.ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVideo) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(messageModel.get(position).getMessage()), "video/mp4");
                    activity.startActivity(intent);
                } else if (messageModel.get(position).getMessageType() == Constants.VIDEO) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(messageModel.get(position).getMessage())), "image/*");
                    activity.startActivity(intent);
                }
            }
        });
    }

    private void configureReceivedAudio(final ReceivedAudioHolder receivedMessageHolder, final int position) {
       /* if (messageModel.get(position).getProgressStatus() == 1) {
            receivedMessageHolder.pbAudio.setVisibility(View.VISIBLE);
        } else {
            receivedMessageHolder.pbAudio.setVisibility(View.GONE);
        }

        chatAudioPlayer = new MediaPlayer();
        chatAudioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            chatAudioPlayer.setDataSource(messageModel.get(position).getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        *//*receivedMessageHolder.ivAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopIfDifferentAudioRunning(position);
                chatAudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        receivedMessageHolder.tvAudio.setText(milliSecondsToTimer(chatAudioPlayer.getDuration()));
                        tvChatAudioTime = receivedMessageHolder.tvAudio;
                    }
                });
                chatAudioPlayer.prepareAsync();
                audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                // Request focus for music stream and pass AudioManager.OnAudioFocusChangeListener
                // implementation reference
                int result = audioManager.requestAudioFocus(ChatAdapter.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Play
                    sbChatAudio = receivedMessageHolder.sbAudio;
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    updateProgressBar();
                    if (chatAudioPlayer.isPlaying()) {
                        if (chatAudioPlayer != null) {
                            chatAudioPlayer.pause();
                            // Changing button image to play button
                            receivedMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                        } else {
                            chatAudioPlayer.release();
                            chatAudioPlayer = null;
                        }
                    } else {
                        if (chatAudioPlayer != null) {
                            chatAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    sbChatAudio = null;
                                    receivedMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                                    receivedMessageHolder.sbAudio.setProgress(0);
                                }
                            });
                            chatAudioPlayer.start();
                        } else {
                            chatAudioPlayer.release();`
                            chatAudioPlayer = null;
                        }
                        // Changing button image to pause button
                        receivedMessageHolder.ivAudio.setImageResource(R.drawable.ic_pause);
                    }
                }
            }
        });*//*

        receivedMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());*/
    }

    private void configureSentText(final SentTextHolder sentMessageHolder, final int position) {
        sentMessageHolder.tvMessage.setText(messageModel.get(position).getMessage());
        sentMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());
    }

    private void configureSentImage(final SentImageHolder sentMessageHolder, final int position, final Boolean isVideo) {
        Bitmap bitmap = BitmapFactory.decodeFile(messageModel.get(position).getThumbNail());
        sentMessageHolder.ivMessage.setImageBitmap(bitmap);
        if (messageModel.get(position).getProgressStatus() == 1) {
            sentMessageHolder.pbImage.setVisibility(View.VISIBLE);
        } else {
            sentMessageHolder.pbImage.setVisibility(View.GONE);
        }

        sentMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());

        if (isVideo) {
            sentMessageHolder.ivPlay.setVisibility(View.VISIBLE);
        }

        sentMessageHolder.ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVideo) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(messageModel.get(position).getMessage()), "video/mp4");
                    activity.startActivity(intent);
                } else if (messageModel.get(position).getMessageType() == Constants.VIDEO) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(messageModel.get(position).getMessage())), "image/*");
                    activity.startActivity(intent);
                }
            }
        });
    }

    private void configureSentAudio(final SentAudioHolder sentMessageHolder, final int position) {
        if (messageModel.get(position).getProgressStatus() == 1) {
            sentMessageHolder.pbAudio.setVisibility(View.VISIBLE);
        } else {
            sentMessageHolder.pbAudio.setVisibility(View.GONE);
        }

        sentMessageHolder.audioPlayer = new MediaPlayer();
        sentMessageHolder.audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            sentMessageHolder.audioPlayer.setDataSource(messageModel.get(position).getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        sentMessageHolder.audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                sentMessageHolder.audioDuration = milliSecondsToTimer(sentMessageHolder.audioPlayer.getDuration());
                sentMessageHolder.tvAudio.setText(sentMessageHolder.audioDuration);
            }
        });
        sentMessageHolder.audioPlayer.prepareAsync();

        sentMessageHolder.ivAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                // Request focus for music stream and pass AudioManager.OnAudioFocusChangeListener
                // implementation reference
                int result = audioManager.requestAudioFocus(ChatAdapter.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (chatPosition == -1) {
                        // TODO: 13/04/17 first time audio
                        //set details
                        tvChatAudioTime = sentMessageHolder.tvAudio;
                        sbChatAudio = sentMessageHolder.sbAudio;
                        ivChatAudio = sentMessageHolder.ivAudio;
                        chatPosition = position;
//                        mHandler.removeCallbacks(mUpdateTimeTask); // TODO: 14/04/17 remove callbacks
                        // TODO: 13/04/17 play the content file
                        chatAudioPlayer = sentMessageHolder.audioPlayer;
                        chatAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mHandler.removeCallbacksAndMessages(null);
                                sentMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                                sentMessageHolder.sbAudio.setProgress(0);

                                chatPosition = -1;
                                sbChatAudio = null;
                                chatAudioPlayer = null;
                            }
                        });
                        updateProgressBar();
                        chatAudioPlayer.start();
                        ivChatAudio.setImageResource(R.drawable.ic_pause);
                    } else {
                        if (chatPosition == position) {
                            // TODO: 13/04/17  same file play button pressed
                            if (chatAudioPlayer.isPlaying()) {
                                chatAudioPlayer.pause();
                                // Changing button image to play button
                                sentMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                            } else {
                                chatAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mHandler.removeCallbacksAndMessages(null);
                                        sbChatAudio = null;
                                        sentMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                                        sentMessageHolder.sbAudio.setProgress(0);
                                        chatPosition = -1;
                                        chatAudioPlayer = null;
                                    }
                                });
                                updateProgressBar();
                                chatAudioPlayer.start();
                                // Changing button image to pause button
                                sentMessageHolder.ivAudio.setImageResource(R.drawable.ic_pause);
                            }
                        } else {
                            // TODO: 13/04/17 difference file play button pressed need to verify
                            mHandler.removeCallbacksAndMessages(null);
                            ivChatAudio.setImageResource(R.drawable.ic_play);
                            sbChatAudio.setProgress(0);

                            chatPosition = -1;
                            sbChatAudio = null;
                            chatAudioPlayer = null;

                            tvChatAudioTime = sentMessageHolder.tvAudio;
                            sbChatAudio = sentMessageHolder.sbAudio;
                            ivChatAudio = sentMessageHolder.ivAudio;
                            chatPosition = position;

                            chatAudioPlayer = sentMessageHolder.audioPlayer;
                            chatAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mHandler.removeCallbacksAndMessages(null);
                                    sentMessageHolder.ivAudio.setImageResource(R.drawable.ic_play);
                                    sentMessageHolder.sbAudio.setProgress(0);

                                    chatPosition = -1;
                                    sbChatAudio = null;
                                    chatAudioPlayer = null;
                                }
                            });
                            updateProgressBar();
                            chatAudioPlayer.start();
                            ivChatAudio.setImageResource(R.drawable.ic_pause);
                        }
                    }
                }
            }
        });
        sentMessageHolder.tvTime.setText(messageModel.get(position).getFormattedTime());
    }

    @Override
    public int getItemCount() {
        return messageModel != null ? messageModel.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messageModel.get(position);
        if (message.getType() == Constants.SENT) {
            switch (message.getMessageType()) {
                case Constants.TEXT:
                    return 1;
                case Constants.IMAGE:
                    return 3;
                case Constants.VIDEO:
                    return 5;
                case Constants.AUDIO:
                    return 7;
            }
        } else if (message.getType() == Constants.RECIEVED) {
            switch (message.getMessageType()) {
                case Constants.TEXT:
                    return 2;
                case Constants.IMAGE:
                    return 4;
                case Constants.VIDEO:
                    return 6;
                case Constants.AUDIO:
                    return 8;
            }
        }
        return -1;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacksAndMessages(null);
        int totalDuration = chatAudioPlayer.getDuration();
        int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        chatAudioPlayer.seekTo(currentPosition);
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.post(mUpdateTimeTask);
    }

    public class SentTextHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public SentTextHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.message_text_view);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public class SentImageHolder extends RecyclerView.ViewHolder {
        private AudioManager audioManager;
        private TextView tvMessage;
        private TextView tvTime;
        private ImageView ivMessage;
        private RelativeLayout rlImage;
        private ProgressBar pbImage;
        private ImageView ivPlay;
        private String audioDuration;

        public SentImageHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.message_text_view);
            ivMessage = (ImageView) itemView.findViewById(R.id.iv_image);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
            rlImage = (RelativeLayout) itemView.findViewById(R.id.rl_image);
            pbImage = (ProgressBar) itemView.findViewById(R.id.pb_image);
            ivPlay = (ImageView) itemView.findViewById(R.id.iv_play);
        }
    }

    public class SentAudioHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlAudio;
        private ImageView ivAudio;
        private SeekBar sbAudio;
        private ProgressBar pbAudio;
        private TextView tvAudio;
        private TextView tvTime;
        private String audioDuration;
        private MediaPlayer audioPlayer;

        public SentAudioHolder(View itemView) {
            super(itemView);
            rlAudio = (RelativeLayout) itemView.findViewById(R.id.rl_audio);
            ivAudio = (ImageView) itemView.findViewById(R.id.iv_audio);
            sbAudio = (SeekBar) itemView.findViewById(R.id.sb_audio);
            pbAudio = (ProgressBar) itemView.findViewById(R.id.pb_audio);
            tvAudio = (TextView) itemView.findViewById(R.id.tv_audio);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public class ReceivedTextHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public ReceivedTextHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.message_text_view);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }

    }

    public class ReceivedImageHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;
        private ImageView ivMessage;
        private RelativeLayout rlImage;
        private ProgressBar pbImage;
        private ImageView ivPlay;

        public ReceivedImageHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.message_text_view);
            ivMessage = (ImageView) itemView.findViewById(R.id.iv_image);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
            rlImage = (RelativeLayout) itemView.findViewById(R.id.rl_image);
            pbImage = (ProgressBar) itemView.findViewById(R.id.pb_image);
            ivPlay = (ImageView) itemView.findViewById(R.id.iv_play);
        }

    }

    public class ReceivedAudioHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlAudio;
        private ImageView ivAudio;
        private SeekBar sbAudio;
        private ProgressBar pbAudio;
        private TextView tvAudio;
        private TextView tvTime;

        public ReceivedAudioHolder(View itemView) {
            super(itemView);
            rlAudio = (RelativeLayout) itemView.findViewById(R.id.rl_audio);
            ivAudio = (ImageView) itemView.findViewById(R.id.iv_audio);
            sbAudio = (SeekBar) itemView.findViewById(R.id.sb_audio);
            pbAudio = (ProgressBar) itemView.findViewById(R.id.pb_audio);
            tvAudio = (TextView) itemView.findViewById(R.id.tv_audio);
            tvTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }

    }

    public void swap(ArrayList<MessageModel> messageModel) {
        this.messageModel = new ArrayList<>();
        this.messageModel.addAll(messageModel);
        notifyDataSetChanged();
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = chatAudioPlayer.getDuration();
            long currentDuration = chatAudioPlayer.getCurrentPosition();

            // Displaying time completed playing
            tvChatAudioTime.setText(milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            if (sbChatAudio != null) {
                sbChatAudio.setProgress(progress);
            }
            mHandler.post(this);
        }
    };

    public void removeHandlerCallBacks() {
        mHandler.removeCallbacksAndMessages(null);
    }


    // Implements AudioManager.OnAudioFocusChangeListener
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            // Pause
            Log.d("AUDIO FOCUS : ", "AUDIOFOCUS_LOSS_TRANSIENT : Pause");
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume
            Log.d("AUDIO FOCUS : ", "AUDIOFOCUS_GAIN : Resume");
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop or pause depending on your need
            Log.d("AUDIO FOCUS : ", "AUDIOFOCUS_LOSS : Stop or pause depending on your need");
            chatAudioPlayer.pause();
            // Changing button image to play button
//            stopAudioRunning();
        }
    }

 /*   private void stopAudioRunning() {
//        chatAudioPlayer.stop();
        sbChatAudio.setProgress(0);
        ivChatAudio.setImageResource(R.drawable.ic_play);
        tvChatAudioTime.setText(chatTimeStamp);
    }

    private void saveCurrentAudioDetailsForReceived(int position, ReceivedAudioHolder receivedMessageHolder, String duration) {
        ivChatAudio = receivedMessageHolder.ivAudio;
        sbChatAudio = receivedMessageHolder.sbAudio;
        tvChatAudioTime = receivedMessageHolder.tvAudio;
        chatTimeStamp = duration;
        chatPosition = position;
    }

    private void saveCurrentAudioDetailsForSent(int position, SentAudioHolder sentMessageHolder, String duration) {
        sbChatAudio = sentMessageHolder.sbAudio;
        ivChatAudio = sentMessageHolder.ivAudio;
        tvChatAudioTime = sentMessageHolder.tvAudio;
        chatTimeStamp = duration;
        chatPosition = position;
    }*/
}

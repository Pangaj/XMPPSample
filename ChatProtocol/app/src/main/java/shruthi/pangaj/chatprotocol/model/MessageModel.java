package shruthi.pangaj.chatprotocol.model;

import android.text.format.DateFormat;

import java.util.concurrent.TimeUnit;

/**
 * Created by Pangaj on 31/03/17.
 */

public class MessageModel {
    private String message;
    private String thumbNail;
    private long timestamp;
    private int messageType;
    private int type;
    private int progressStatus;

    public MessageModel(String message,String thumbNail, long timestamp, int type, int messageType, int progressStatus) {
        this.message = message;
        this.thumbNail = thumbNail;
        this.timestamp = timestamp;
        this.type = type;                   //sent or received
        this.messageType = messageType;     //text or image
        this.progressStatus = progressStatus;     //progress bar for image
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    public int getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(int progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getFormattedTime() {

        long oneDayInMillis = TimeUnit.DAYS.toMillis(1); // 24 * 60 * 60 * 1000;

        long timeDifference = System.currentTimeMillis() - timestamp;

        return timeDifference < oneDayInMillis
                ? DateFormat.format("hh:mm a", timestamp).toString()
                : DateFormat.format("dd MMM - hh:mm a", timestamp).toString();
    }
}

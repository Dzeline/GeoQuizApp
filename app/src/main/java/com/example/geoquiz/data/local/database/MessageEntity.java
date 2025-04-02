package com.example.geoquiz.data.local.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single chat message in the SMS-based messaging system.
 *  Each message is stored in the local Room database with sender, receiver, content, and timestamp.
 */
@Entity(tableName = "Messages")
public class MessageEntity  {

    /**
     *Unique identifier for each message (auto-incremented by Room).
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     *Sender of the message ("You" if outgoing, phone number if incoming).
     */
    @ColumnInfo(name = "sender")
    public String sender;

    /**
     *Content of the message (plain text).
     */
    @ColumnInfo(name = "message")
    public String message;

    /**
     * Time the message was sent/received, stored as Unix timestamp (milliseconds).
     */
    @ColumnInfo(name = "timestamp")
    public long timestamp;
    /**
     * Receiver of the message (phone number).
     */
    @ColumnInfo(name = "receiver")
    public String receiver;

    /**
     * Constructor for creating a message entry.
     *
     * @param sender    Who sent the message (e.g. "You" or contact phone number)
     * @param receiver  Who receives the message (e.g. recipient phone number)
     * @param message   Message text content
     * @param timestamp Unix timestamp in milliseconds
     */

    public MessageEntity(String sender,String receiver, String message, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }
    /**
     * Gets the sender of the message.
     * @return the sender's identifier
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the message content.
     * @return message text
     */
    public String getMessage() {
        return message;
    }
    /**
     * Gets the timestamp of the message.
     * @return time in milliseconds since epoch
     */
    public long getTimestamp(){
        return timestamp;
    }
    /**
     * Gets the receiver of the message.
     * @return receiver's phone number
     */
    public String getReceiver() {
        return receiver;
    }
}

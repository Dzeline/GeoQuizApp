package com.example.geoquiz.data.local.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class MessageDao {
    @Insert
    public abstract long insertMessage(MessageEntity message);

    @Query("SELECT * FROM Messages ORDER BY timestamp ASC")
    public abstract LiveData<List<MessageEntity>> getAllMessages();

    @Query("SELECT * FROM Messages WHERE sender = :sender AND receiver = :receiver ORDER BY timestamp ASC")
    public abstract LiveData<List<MessageEntity>> getMessagesBetween(String sender, String receiver);

    /**
     * Retrieves all messages where the given contact is either sender or receiver.
     *
     * @param contact Phone number of the contact to filter messages with
     * @return LiveData stream of message list
     */
    @Query("SELECT * FROM Messages WHERE sender = :contact OR receiver = :contact ORDER BY timestamp ASC")
    public abstract LiveData<List<MessageEntity>> getMessagesForContact(String contact);


}

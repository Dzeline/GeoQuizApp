package com.example.geoquiz.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.geoquiz.data.local.database.MessageEntity;

import java.util.List;
public interface MessageRepository {
    void insertMessage(MessageEntity message);
    LiveData<List<MessageEntity>> getAllMessages();
    LiveData<List<MessageEntity>> getMessagesForContact(String contactPhone);

}

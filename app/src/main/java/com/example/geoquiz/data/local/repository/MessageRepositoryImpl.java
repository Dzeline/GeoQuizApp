package com.example.geoquiz.data.local.repository;

import androidx.lifecycle.LiveData;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;

import java.util.List;

import javax.inject.Inject;

public class MessageRepositoryImpl implements MessageRepository {

    private final GeoQuizDatabase db;

    @Inject
    public MessageRepositoryImpl(GeoQuizDatabase db) {
       this.db =db ;
    }

    @Override
    public void insertMessage(MessageEntity message) {
        GeoQuizDatabase.databaseWriteExecutor.execute(() -> db.messageDao().insertMessage(message));
    }

    @Override
    public LiveData<List<MessageEntity>> getAllMessages() {
        return  db.messageDao().getAllMessages();
    }
    @Override
    public LiveData<List<MessageEntity>> getMessagesForContact(String contactPhone) {
        return db.messageDao().getMessagesForContact(contactPhone);
    }

}

package com.example.geoquiz.data.local.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

//import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.MessageDao;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageDao messageDao; // Changed from GeoQuizDatabase
    private final ExecutorService ioExecutor;

    @Inject
    public MessageRepositoryImpl(MessageDao messageDao, @Named("ioExecutor") ExecutorService ioExecutor) {
        this.messageDao = messageDao;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public void insertMessage(MessageEntity message) {
        if (message == null ||
                message.getMessage() == null ||
                message.getSender() == null ||
                message.getReceiver() == null) {
            Log.e("MessageRepository", "Attempted to insert null message — skipped.");
            return;
        }
        ioExecutor.execute(() -> messageDao.insertMessage(message));
    }

    @Override
    public LiveData<List<MessageEntity>> getAllMessages() {
        return messageDao.getAllMessages();
    }
    @Override
    public LiveData<List<MessageEntity>> getMessagesForContact(String contactPhone) {
        return messageDao.getMessagesForContact(contactPhone);
    }

}

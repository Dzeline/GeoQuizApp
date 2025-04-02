package com.example.geoquiz.presentation.feature_rider;


import androidx.lifecycle.LiveData;

import androidx.lifecycle.ViewModel;

import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;


import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for RiderActivity. Manages access to chat messages via repository.
 */
@HiltViewModel
public class RiderViewModel   extends ViewModel{
    private final MessageRepository messageRepo;

    /**
     * Constructs the ViewModel with a provided MessageRepository.
     *
     * @param messageRepo Repository to access chat messages
     */
    @Inject
    public RiderViewModel(MessageRepository messageRepo) {
        this.messageRepo = messageRepo;

    }

    /**
     * Exposes a LiveData list of all messages in the database.
     *
     * @return LiveData list of messages
     */
    public LiveData<List<MessageEntity>> getMessages() {
        return messageRepo.getAllMessages();
    }

    /**
     * Sends a new message using the repository.
     *
     * @param sender Sender identifier (e.g. "You")
     * @param message Message content
     */
    public void sendMessage(String sender, String message) {
        messageRepo.insertMessage(new MessageEntity(sender, "unknown",message, System.currentTimeMillis()));

    }


}

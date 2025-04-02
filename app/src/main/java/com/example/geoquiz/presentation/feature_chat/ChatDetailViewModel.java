package com.example.geoquiz.presentation.feature_chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for chatDetailActivity.
 * Filters and provides messages specific to a given contact/rider.
 */
@HiltViewModel
public class ChatDetailViewModel extends ViewModel  {

    private final MessageRepository messageRepo;

    /**
     * Constructor that injects the message repository.
     *
     * @param messageRepo repository that handles message queries
     */
    @Inject
    public ChatDetailViewModel(MessageRepository messageRepo) {
         this.messageRepo = messageRepo;
    }


    /**
     * Returns all messages exchanged with the specified contact.
     *
     * @param contactPhone the contact's phone number
     * @return LiveData stream of messages
     */
    public LiveData<List<MessageEntity>> getMessagesForContact(String contactPhone) {
        return messageRepo.getMessagesForContact(contactPhone);
    }

    public void sendMessage(String sender, String receiver, String messageText) {
        MessageEntity message = new MessageEntity(sender, receiver, messageText, System.currentTimeMillis());
        messageRepo.insertMessage(message);
    }


}

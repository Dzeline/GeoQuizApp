package com.example.geoquiz.presentation.feature_chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.domain.repository.MessageRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainFunctionViewModel  extends ViewModel {

    private final  MessageRepository messageRepo;
    private final LiveData<List<MessageEntity>> messages;




    @Inject
    public MainFunctionViewModel(MessageRepository messageRepo) {

        this.messageRepo =messageRepo;
        this.messages = (LiveData<List<MessageEntity>>) messageRepo.getAllMessages();


    }

    public LiveData<List<MessageEntity>> getMessages() {
        return messages;
    }
    public void insertMessage(MessageEntity message) {
        messageRepo.insertMessage(message);
    }


}

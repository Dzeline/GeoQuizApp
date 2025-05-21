package com.example.geoquiz.presentation.feature_chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.data.local.database.RiderEntity;
import com.example.geoquiz.domain.model.RiderInfo;
import com.example.geoquiz.domain.repository.MessageRepository;
import com.example.geoquiz.domain.repository.RiderRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainFunctionViewModel  extends ViewModel {

    private final  MessageRepository messageRepo;
    private  LiveData<List<MessageEntity>> messages;

    private final RiderRepository riderRepo;

    private String currentRiderPhone = null;

    @Inject
    public MainFunctionViewModel(MessageRepository messageRepo,RiderRepository riderRepo) {

        this.messageRepo =messageRepo;
        this.messages = messageRepo.getAllMessages();
        this.riderRepo= riderRepo;


    }

    public LiveData<List<MessageEntity>> getMessages() {
        return messages;
    }
    public void insertMessage(MessageEntity message) {
        messageRepo.insertMessage(message);
    }
    /**
          * Load messages for a specific rider
          * @param riderPhone The phone number of the rider to load messages for
          */
    public void loadMessagesForRider(String riderPhone) {
                if (riderPhone != null && !riderPhone.isEmpty()) {
                       currentRiderPhone = riderPhone;
                        // Update the messages LiveData to only show messages for the selected rider
                               messages = messageRepo.getMessagesForContact(riderPhone);
                   } else {
                        // If no rider is selected, show all messages
                                messages = messageRepo.getAllMessages();
                   }
    }
    /**
     * Get the currently selected rider's phone number
     * @return The phone number of the currently selected rider, or null if none is selected
     */
   public String getCurrentRiderPhone() {
               return currentRiderPhone;
    }


    /** Live list of domain-level RiderInfo for the UI */
    public LiveData<List<RiderInfo>> getAvailableRiders() {
        return Transformations.map(
                riderRepo.getAllRiders(),
                entities -> {
                    List<RiderInfo> out = new ArrayList<>();
                    for (RiderEntity e : entities) {
                        out.add(new RiderInfo(
                                e.phoneNumber,      // use phone # as name for now or lookup a better label
                                "",                 // no last message yet
                                e.isAvailable,
                                R.drawable.ic_profile_placeholder,
                                e.phoneNumber
                        ));
                    }
                    return out;
                }
        );
    }
}

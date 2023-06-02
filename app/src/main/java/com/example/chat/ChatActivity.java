package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.User;
import com.example.chat.utilities.Constantes;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceivedDetails();
    }
    private void loadReceivedDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(Constantes.KEY_USER);
        binding.nomeText.setText(receiverUser.nome);
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}
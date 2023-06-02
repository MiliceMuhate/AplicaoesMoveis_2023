package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat.adapters.UsersAdapter;
import com.example.chat.databinding.ActivityUsersBinding;
import com.example.chat.listeners.UserListener;
import com.example.chat.models.User;
import com.example.chat.utilities.Constantes;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constantes.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentId=preferenceManager.getString(Constantes.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User> users=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.nome=queryDocumentSnapshot.getString(Constantes.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(Constantes.KEY_EMAIL);
                            user.imagem=queryDocumentSnapshot.getString(Constantes.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constantes.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if(users.size()>0){
                            UsersAdapter adapter=new UsersAdapter(users,this);
                            binding.usersRecycleView.setAdapter(adapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","Nenhum usuario encontrado"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLading){
        if(isLading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constantes.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}
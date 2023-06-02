package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chat.databinding.ActivitySingInBinding;
import com.example.chat.utilities.Constantes;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SingInActivity extends AppCompatActivity {
    private ActivitySingInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager=new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constantes.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding=ActivitySingInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.signIn.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signIn();
            }
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constantes.KEY_COLLECTION_USERS)
                .whereEqualTo(Constantes.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constantes.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constantes.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constantes.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constantes.KEY_NAME,documentSnapshot.getString(Constantes.KEY_NAME));
                        preferenceManager.putString(Constantes.KEY_IMAGE,documentSnapshot.getString(Constantes.KEY_IMAGE));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("Impossivel fazer o login");
                    }
                });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.signIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.signIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Inttroduza o email !");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Introduza um email valido");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Introduza a palavra passe");
            return false;
        }
        return true;
    }

}


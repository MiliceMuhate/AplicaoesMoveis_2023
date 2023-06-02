package com.example.chat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chat.databinding.ActivitySignUpBinding;
import com.example.chat.utilities.Constantes;
import com.example.chat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setListeners();
    }
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.signUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            picImage.launch(intent);
        });
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String, Object>user=new HashMap<>();
        user.put(Constantes.KEY_NAME,binding.name.getText().toString());
        user.put(Constantes.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constantes.KEY_PASSWORD,binding.inputPassword.getText().toString());
        user.put(Constantes.KEY_IMAGE,encodeImage);
        database.collection(Constantes.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constantes.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constantes.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constantes.KEY_NAME,binding.name.getText().toString());
                    preferenceManager.putString(Constantes.KEY_IMAGE,encodeImage);
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception->{

                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher picImage=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode()==RESULT_OK){
                    Uri imageUri=result.getData().getData();
                    try {
                        InputStream inputStream=getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        binding.imageProfile.setImageBitmap(bitmap);
                        binding.textAddImage.setVisibility(View.GONE);
                        encodeImage=encodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
    );


    private boolean isValidSignUpDetails(){
        if(encodeImage==null){
            showToast("Selecione uma imagem de perfil !");
            return false;
        } else if (binding.name.getText().toString().trim().isEmpty()) {
                showToast("Nome nao pode estar vazio !");
                return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Email nao pode estar vazio !");
            return false;
        }else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Password nao pode estar vazio !");
            return false;
        }else if (binding.inputConfirmPassword.getText().toString()==binding.inputPassword.getText().toString()) {
            showToast("Palavras passe nao conscidem !");
            return false;
        }else{
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.signUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.signUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
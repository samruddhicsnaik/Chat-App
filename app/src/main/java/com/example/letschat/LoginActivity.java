package com.example.letschat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity
{
    private FirebaseAuth firebaseAuth;
    private TextInputLayout unameInput, passwdInput;
    private Button loginNext;
    private ProgressDialog progressBar;
    private TextView forgotPassword;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        progressBar = new ProgressDialog(this);
        unameInput = findViewById(R.id.loginUsername);
        passwdInput = findViewById(R.id.loginPassword);
        forgotPassword = findViewById(R.id.forgot);
        loginNext = findViewById(R.id.login);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgot(v);
            }
        });
        loginNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String username = unameInput.getEditText().getText().toString().trim();
                String password = passwdInput.getEditText().getText().toString().trim();
                if(username.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Enter Username and Password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    progressBar.setTitle("Trying to Login");
                    progressBar.setMessage("Please Wait");
                    progressBar.setCanceledOnTouchOutside(true);
                    progressBar.show();
                    firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                if(firebaseAuth.getCurrentUser().isEmailVerified())
                                {
                                    String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                    userRef.child(currentUserID).child("device_token")
                                            .setValue(deviceToken)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        progressBar.dismiss();
                                                        mainActivity();
                                                    }
                                                }
                                            });
                                }
                                else
                                {
                                    progressBar.dismiss();
                                    Toast.makeText(LoginActivity.this, "verify email", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " +message, Toast.LENGTH_SHORT).show();
                                progressBar.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private void mainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void forgot(View view)
    {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    public void createAccount(View view)
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }
}

package com.example.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity
{
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth firebaseAuth;
    private Button verify;
    private TextInputLayout verificationCode;
    private DatabaseReference rootRef;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private TextInputLayout emailInput, passwdInput;
    private TextView loginLink, createAccount;
    private Button emailNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        emailInput = findViewById(R.id.email_register);
        passwdInput = findViewById(R.id.password_register);
        loginLink = findViewById(R.id.login_link);
        emailNext = findViewById(R.id.email_next);
        createAccount = findViewById(R.id.create_account);
        loginLink.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
        );
        emailNext.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String username = emailInput.getEditText().getText().toString().trim();
                        String password = passwdInput.getEditText().getText().toString().trim();

                        if(username.isEmpty() || password.isEmpty() )
                        {
                            Toast.makeText(RegistrationActivity.this, "Enter Username and Password", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            firebaseAuth.createUserWithEmailAndPassword(username, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                firebaseAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    Toast.makeText(RegistrationActivity.this, "Check Email for Verification", Toast.LENGTH_SHORT).show();

                                                                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                            else
                                            {
                                                String message = task.getException().toString();
                                                Toast.makeText(RegistrationActivity.this, "Error : " +message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void loginActivity()
    {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mainActivity()
    {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

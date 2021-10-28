package com.example.letschat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class EmailRegisterFragment extends Fragment
{
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private TextInputLayout emailInput, passwdInput;
    private Button emailNext, createAccount;
    private ProgressDialog loadingBar;
    private TextView loginLink;

    public EmailRegisterFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_email_register, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(getActivity());

        emailInput = view.findViewById(R.id.email_register);
        passwdInput = view.findViewById(R.id.password_register);
        loginLink = view.findViewById(R.id.login_link);
        emailNext = view.findViewById(R.id.email_next);
        createAccount = view.findViewById(R.id.create_account);

        loginLink.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
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
                            Toast.makeText(getActivity(), "Enter Username and Password", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            /*loadingBar.setTitle("Creating Account");
                            loadingBar.setMessage("Please wait");
                            loadingBar.setCanceledOnTouchOutside(true);
                            loadingBar.show();*/

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
                                                            Intent intent = new Intent(EmailRegisterFragment.this.getActivity(), RegistrationActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);

                                                            /*emailNext.setVisibility(View.GONE);
                                                            createAccount.setVisibility(View.VISIBLE);

                                                            Toast.makeText(getActivity(), "Please check Email for verification", Toast.LENGTH_SHORT).show();

                                                            createAccount.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if(firebaseAuth.getCurrentUser().isEmailVerified())
                                                                    {
                                                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                                                        String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                                                        rootRef.child("Users").child(currentUserID).setValue("");

                                                                        rootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                                                                        Toast.makeText(getActivity(), "Account created successfully", Toast.LENGTH_SHORT).show();
                                                                        loadingBar.dismiss();
                                                                        loginActivity();
                                                                    }
                                                                    else
                                                                    {
                                                                        Toast.makeText(getActivity(), "Please verify Email Address", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });*/

                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });


                                        /**/
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(getActivity(), "Error : " +message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });
        return view;
    }

    private void loginActivity()
    {
        Intent intent = new Intent(EmailRegisterFragment.this.getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void mainActivity()
    {
        Intent intent = new Intent(EmailRegisterFragment.this.getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}

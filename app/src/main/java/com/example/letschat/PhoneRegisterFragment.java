package com.example.letschat;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneRegisterFragment extends Fragment
{
    private Button register;
    private TextInputLayout phoneNumber;


    public PhoneRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_phone_register, container, false);

        phoneNumber = view.findViewById(R.id.phone_number_register);
        register = view.findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phone = phoneNumber.getEditText().getText().toString().trim();

                if(phone.isEmpty())
                {
                    Toast.makeText(getContext(), "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(getContext(), RegistrationActivity.class);
                    intent.putExtra("phone number", phone);
                    startActivity(intent);
                }

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}

package com.example.letschat;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class MessageSchedular extends Worker {

    public MessageSchedular(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork()
    {
        String msg = getInputData().getString("msg");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map messageDetails= new HashMap();
        Gson gson= new Gson();
        messageDetails = gson.fromJson(msg,Map.class);
        rootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

            }
        });
        return Result.success();
    }
}
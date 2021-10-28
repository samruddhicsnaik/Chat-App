package com.example.letschat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder>
{
    public static final int sender_msg = 0;
    public static final int receiver_msg = 1;
    private List<GroupMessages> messagesList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, userRef;
    private String currentUserID;

    public GroupMessageAdapter(List<GroupMessages> messagesList)
    {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_message_layout, parent, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return new GroupMessageViewHolder(view);*/

        if(viewType == sender_msg)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_layout, parent, false);
            firebaseAuth = FirebaseAuth.getInstance();
            return new GroupMessageViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_layout, parent, false);
            firebaseAuth = FirebaseAuth.getInstance();
            return new GroupMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder holder, int position)
    {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        final GroupMessages messages = messagesList.get(position);
        rootRef = FirebaseDatabase.getInstance().getReference();
        String fromMessageType = messages.getType();
        String fromUserID = messages.getUser();


        holder.message_layout.setVisibility(View.GONE);
        holder.file_layout.setVisibility(View.GONE);
        holder.date.setVisibility(View.GONE);
        holder.name.setVisibility(View.VISIBLE);

        long previousTs = 0;
        if(position>=1)
        {
            GroupMessages pm = messagesList.get(position-1);
            previousTs = Long.parseLong(pm.getDate());
        }
        //setTimeTextVisibility(Long.parseLong(messages.getDate()), previousTs, holder.date);

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(Long.parseLong(messagesList.get(position).getDate()));
        cal2.setTimeInMillis(previousTs);

        boolean sameMonth = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

        if(!sameMonth)
        {
            holder.date.setVisibility(View.VISIBLE);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            String dateString = formatter.format(new Date(Long.parseLong(messagesList.get(position).getDate())));
            holder.date.setText(dateString);
        }

        if(fromMessageType.equals("text")) {
            holder.message_layout.setVisibility(View.VISIBLE);

            holder.message.setText(messages.getMessage());
            holder.time.setText(messages.getTime());
            holder.seen.setText("Delivered");

            if (messages.isSeen()) {
                holder.seen.setText("Seen");
            } else {
                holder.seen.setText("Delivered");
            }

            rootRef.child("Users").child(messages.getUser()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final String name = dataSnapshot.child("name").getValue().toString();
                    holder.name.setText(name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        /*String name;
        final String currentUserID = firebaseAuth.getCurrentUser().getUid();
        final GroupMessages messages = messagesList.get(position);
        final String fromUserID = messages.getUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(fromUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final String name = dataSnapshot.child("name").getValue().toString();
                holder.receiverMessage.setVisibility(View.GONE);
                holder.senderMessage.setVisibility(View.GONE);
                if(fromUserID.equals(currentUserID))
                {
                    holder.senderMessage.setVisibility(View.VISIBLE);
                    holder.senderMessage.setText(name + "\n\n" + messages.getMessage() + "\n\n" + messages.getTime());
                }
                else
                {
                    holder.receiverMessage.setVisibility(View.VISIBLE);
                    holder.receiverMessage.setText(name + "\n\n" + messages.getMessage() + "\n\n" + messages.getTime());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });*/

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        if(messagesList.get(position).getUser().equals(currentUserID))
        {
            return sender_msg;
        }
        else
        {
            return receiver_msg;
        }
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder
    {
        //public TextView senderMessage, receiverMessage;
        public RelativeLayout message_layout, file_layout;
        public TextView message, time, seen, file_time, file_seen, date, name, sender_name;
        public ImageView file;
        public GroupMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            seen = itemView.findViewById(R.id.seen);
            file = itemView.findViewById(R.id.file);
            file_time = itemView.findViewById(R.id.file_time);
            file_seen = itemView.findViewById(R.id.file_seen);
            message_layout = itemView.findViewById(R.id.message_layout);
            file_layout = itemView.findViewById(R.id.file_layout);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.name);
            sender_name = itemView.findViewById(R.id.sender_name);
            //senderMessage = itemView.findViewById(R.id.sender_message);
            //receiverMessage = itemView.findViewById(R.id.receiver_message);
        }
    }
}

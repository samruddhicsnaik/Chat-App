package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>
{
    private Context context;
    private List<Chats> chatsList;
    private DatabaseReference rootRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID, lastMessage,msgSenderStatus, msgReceiverStatus, time, date;


    public ChatsAdapter(Context context, List<Chats> chatsList)
    {
        this.context = context;
        this.chatsList = chatsList;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_display, parent, false);
        ChatsViewHolder viewHolder = new ChatsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {
        final Chats chats = chatsList.get(position);
        final String userID = chats.getUser();

        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null)
        {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        }
        if(!(chats.getUser().equals(currentUserID)))
        {
            rootRef.child("Users").child(chats.getUser()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child("image").exists())
                    {
                        final String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(holder.userImage);
                    }
                    else
                    {
                        holder.userImage.setImageResource(R.drawable.user);
                    }
                    final String name = dataSnapshot.child("name").getValue().toString();
                    //final String about = dataSnapshot.child("about").getValue().toString();
                    holder.userName.setText(name);
                    //holder.userAbout.setText(about);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chat_person_id", chats.getUser());
                            context.startActivity(intent);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });


            lastMessage = "default";

            rootRef.child("Messages").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Messages messages = snapshot.getValue(Messages.class);
                        if(messages.getTo().equals(currentUserID) && messages.getFrom().equals(userID) ||
                                messages.getTo().equals(userID) && messages.getFrom().equals(currentUserID))
                        {
                            lastMessage = messages.getMessage();
                            time = messages.getTime();
                            date = messages.getDate();
                            msgSenderStatus = messages.getSenderStatus();
                            msgReceiverStatus = messages.getReceiverStatus();
                        }
                    }

                    switch (lastMessage)
                    {
                        case "default":
                            holder.lastMessage.setText("");
                            holder.time.setText("");
                            break;
                        default:
                            if(msgSenderStatus.equals("not deleted") || msgReceiverStatus.equals("not deleted"))
                            {
                                holder.lastMessage.setText(lastMessage);

                                Calendar cal1 = Calendar.getInstance();
                                Calendar cal2 = Calendar.getInstance();
                                cal1.setTimeInMillis(Long.parseLong(date));
                                cal2.getTimeInMillis();

                                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

                                if(sameDay)
                                {
                                    holder.time.setText(time);
                                }
                                else
                                {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
                                    String dateString = formatter.format(new Date(Long.parseLong(date)));
                                    holder.time.setText(dateString);
                                }
                                //holder.time.setText(time);
                                break;
                            }


                    }
                    lastMessage = "default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView userImage;
        TextView userName, lastMessage, time;
        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            time = itemView.findViewById(R.id.time);
        }
    }
}

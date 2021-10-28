package com.example.letschat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    public static final int sender_msg = 0;
    public static final int receiver_msg = 1;
    private Context context;
    private List<Messages> messagesList;
    private DatabaseReference rootRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    public MessageAdapter(Context context, List<Messages> messagesList)
    {
        this.context = context;
        this.messagesList = messagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView message, time, seen, file_time, file_seen, date;
        public ImageView file;
        public RelativeLayout message_layout, file_layout;

        public MessageViewHolder(@NonNull View itemView)
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
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(viewType == sender_msg)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_layout, parent, false);
            firebaseAuth = FirebaseAuth.getInstance();
            return new MessageViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_layout, parent, false);
            firebaseAuth = FirebaseAuth.getInstance();
            return new MessageViewHolder(view);
        }
    }

    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText)
    {
        if (ts2 == 0) {
            timeText.setVisibility(View.VISIBLE);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            String dateString = formatter.format(new Date(ts1));
            timeText.setText(dateString);
        }
        else {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(ts1);
            cal2.setTimeInMillis(ts2);

            boolean sameMonth = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

            if (sameMonth) {
                timeText.setVisibility(View.GONE);
                //timeText.setText("");
            }

            else {
                timeText.setVisibility(View.VISIBLE);
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
                String dateString = formatter.format(new Date(ts2));
                timeText.setText(dateString);
                //fr
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        final Messages messages = messagesList.get(position);
        rootRef = FirebaseDatabase.getInstance().getReference();

        String fromMessageType = messages.getType();
        String fromUserID = messages.getFrom();
        String toUserID = messages.getTo();

        Context context = holder.itemView.getContext();

        /*holder.message.setText(messages.getMessage());
        holder.time.setText(messages.getTime());
        holder.seen.setText("Delivered");

        if(messages.isSeen())
        {
            holder.seen.setText("Seen");
        }
        else
        {
            holder.seen.setText("Delivered");
        }*/

        holder.message_layout.setVisibility(View.GONE);
        holder.file_layout.setVisibility(View.GONE);
        holder.date.setVisibility(View.GONE);

        //String previous_date = messagesList.get(messagesList.size()-1).getDate();
        //Toast.makeText(context, "date"+previous_date, Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, ""+messages.getDate(), Toast.LENGTH_SHORT).show();

        /*if(position==0)
        {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(messages.getDate());
        }

        for(int i=0; i<messagesList.size()-1; i++)
        {
            String previous_date = messagesList.get(i).getDate();
            if(!previous_date.equals(messages.getDate()))
            {
                holder.date.setVisibility(View.VISIBLE);
                holder.date.setText(messages.getDate());
            }
        }
*/

        long previousTs = 0;
        if(position>=1)
        {
            Messages pm = messagesList.get(position-1);
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

        /*else if(position==messagesList.size()-1)
        {
            //Toast.makeText(context, "date"+position, Toast.LENGTH_SHORT).show();
            //holder.date.setVisibility(View.VISIBLE);

            String previous_date = messagesList.get(position).getDate();
            String current_date = messages.getDate();

            Toast.makeText(context, "date"+previous_date+current_date, Toast.LENGTH_SHORT).show();
            if(!previous_date.equals(current_date))
            {
                Toast.makeText(context, "diff date", Toast.LENGTH_SHORT).show();
                holder.date.setVisibility(View.VISIBLE);
                holder.date.setText(current_date);
            }

            *//*SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            Date previousStrDate = null;
            try {
                strDate = sdf.parse(previous_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date currentStrDate = sdf.parse(current_date);
            if (System.currentTimeMillis() > strDate.getTime()) {
                catalog_outdated = 1;
            }*//*

        }*/
        /*if(toUserID.equals(currentUserID))
        {
            rootRef.child("Messages").child(messages.getFrom()).child(messages.getTo()).child(messages.getMessageID()).child("seen").setValue(true);
        }
        String fromMessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        holder.receiverMessage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);

        if(toUserID.equals(currentUserID))
        {
            holder.messageSeen.setText("    ✓");
        }*/

        //int itemViewType = getItemViewType(position);
        if(fromMessageType.equals("text"))
        {
            holder.message_layout.setVisibility(View.VISIBLE);

            holder.message.setText(messages.getMessage());
            holder.time.setText(messages.getTime());
            holder.seen.setText("Delivered");

            if(messages.isSeen())
            {
                holder.seen.setText("Seen");
            }
            else
            {
                holder.seen.setText("Delivered");
            }

            /*if(itemViewType==sender_msg && "not deleted".equals(messages.getSenderStatus()))
            {

            }
            if (itemViewType == receiver_msg && "not deleted".equals(messages.getReceiverStatus()))
            {
                holder.message_layout.setVisibility(View.VISIBLE);

                holder.message.setText(messages.getMessage());
                holder.time.setText(messages.getTime());
                holder.seen.setText("Delivered");

                if(messages.isSeen())
                {
                    holder.seen.setText("Seen");
                }
                else
                {
                    holder.seen.setText("Delivered");
                }
             }*/



            /*final String secretKey = "AXXXXXXXXXXXXZ";
            String msg = messages.getMessage();
            String decryptedString = AES.decrypt(msg, secretKey) ;
            if(fromUserID.equals(currentUserID))
            {
                holder.senderMessage.setVisibility(View.VISIBLE);
                holder.senderMessage.setBackgroundResource(R.drawable.sender_message_layout);
                if(messages.isSeen())
                {
                    holder.senderMessage.setText(msg + "\n\n" + messages.getTime());
                    holder.messageSeen.setText("    ✓✓");
                }
                else if(!messages.isSeen())
                {
                    holder.senderMessage.setText(msg + "\n\n" + messages.getTime());
                    holder.messageSeen.setVisibility(View.GONE);
                }
            }
            else
            {
                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessage.setText(msg + "\n\n" + messages.getTime());
            }*/
        }
        else if (fromMessageType.equals("image"))
        {
            holder.file_layout.setVisibility(View.VISIBLE);
            Picasso.get().load(messages.getMessage()).into(holder.file);

            holder.file_time.setText(messages.getTime());
            holder.file_seen.setText("Delivered");
            if(messages.isSeen())
            {
                holder.file_seen.setText("Seen");
            }
            else
            {
                holder.file_seen.setText("Delivered");
            }


            /*if(fromUserID.equals(currentUserID))
            {
                holder.senderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.senderImage);

                holder.senderImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else
            {
                holder.receiverImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.receiverImage);
            }*/
        }
        else if(fromMessageType.equals("pdf") || fromMessageType.equals("doc"))
        {
            holder.file_layout.setVisibility(View.VISIBLE);

            if(fromMessageType.equals("pdf"))
            {
                holder.file.setImageResource(R.drawable.pdf);
            }
            else if(fromMessageType.equals("doc"))
            {
                holder.file.setImageResource(R.drawable.doc);
            }

            holder.file_time.setText(messages.getTime());
            holder.file_seen.setText("Delivered");
            if(messages.isSeen())
            {
                holder.file_seen.setText("Seen");
            }
            else
            {
                holder.file_seen.setText("Delivered");
            }

            /*if(fromUserID.equals(currentUserID))
            {
                holder.senderImage.setVisibility(View.VISIBLE);
                if(fromMessageType.equals("pdf"))
                {
                    holder.senderImage.setImageResource(R.drawable.pdf);
                }
                else if(fromMessageType.equals("doc"))
                {
                    holder.senderImage.setImageResource(R.drawable.doc);
                }
            }*/
            /*else
            {
                holder.receiverImage.setVisibility(View.VISIBLE);
                if(fromMessageType.equals("pdf"))
                {
                    holder.receiverImage.setImageResource(R.drawable.pdf);
                }
                else if(fromMessageType.equals("doc"))
                {
                    holder.receiverImage.setImageResource(R.drawable.doc);
                }
            }*/
        }
        if(fromUserID.equals(currentUserID))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if(messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("doc"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i)
                            {
                                /*if(i==0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }*/
                                if(i==0)
                                {
                                    deleteSentMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                                if(i==1)
                                {
                                    deleteMessageForEveryone(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(messagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0)
                                {
                                    deleteSentMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 1)
                                {
                                    deleteMessageForEveryone(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(messagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download",
                                        "Delete for me",
                                        "Delete for everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 1)
                                {
                                    deleteSentMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 2)
                                {
                                    deleteMessageForEveryone(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }

        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if(messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("doc"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download",
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position)
                            {
                                if(position==0)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position==1)
                                {
                                    deleteReceivedMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(messagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0)
                                {
                                    deleteReceivedMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                    else if(messagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download",
                                        "Delete for me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", messagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (i == 1)
                                {
                                    deleteReceivedMessage(position, holder);
                                    //Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    //holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();
        if(messagesList.get(position).getFrom().equals(currentUserID))
        {
            return sender_msg;
        }
        else
        {
            return receiver_msg;
        }
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(messagesList.get(position).getMessageID())
                .child("senderStatus")
                .setValue("deleted sent message").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    //Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*rootRef.child("Messages")
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(messagesList.get(position).getMessageID())
                .child("receiverStatus")
                .setValue("deleted received message").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    //Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages")
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    //Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*rootRef.child("Messages")
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getTo())
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootRef.child("Messages")
                            .child(messagesList.get(position).getTo())
                            .child(messagesList.get(position).getFrom())
                            .child(messagesList.get(position).getMessageID())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(holder.itemView.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    public static class AES
    {
        private static SecretKeySpec secretKey;
        private static byte[] key;
        public static void setKey(String myKey)
        {
            MessageDigest sha = null;
            try {
                key = myKey.getBytes("UTF-8");
                sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                secretKey = new SecretKeySpec(key, "AES");
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        public static String encrypt(String strToEncrypt, String secret)
        {
            try
            {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
            }
            catch (Exception e)
            {
                System.out.println("Error while encrypting: " + e.toString());
            }
            return null;
        }

        public static String decrypt(String strToDecrypt, String secret)
        {
            try
            {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            }
            catch (Exception e)
            {
                System.out.println("Error while decrypting: " + e.toString());
            }
            return null;
        }
    }
}

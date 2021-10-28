package com.example.letschat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGroupMembersAdapter extends RecyclerView.Adapter<AddGroupMembersAdapter.AddGroupMembersViewHolder>
{
    private Context context;
    private List<String> membersList;

    public AddGroupMembersAdapter(Context context, List<String> membersList)
    {
        this.context = context;
        this.membersList = membersList;
    }

    public class AddGroupMembersViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView personImage;
        TextView personName, personAbout;

        public AddGroupMembersViewHolder(@NonNull View itemView)
        {
            super(itemView);

            personImage = itemView.findViewById(R.id.user_image);
            personName = itemView.findViewById(R.id.user_name);
            personAbout = itemView.findViewById(R.id.user_about);
        }
    }


    @NonNull
    @Override
    public AddGroupMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display, parent, false);
        AddGroupMembersViewHolder viewHolder = new AddGroupMembersViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddGroupMembersViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }
}

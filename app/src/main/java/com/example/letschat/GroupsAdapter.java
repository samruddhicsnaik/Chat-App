package com.example.letschat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>
{
    private Context context;
    private List<Group> groupsList;
    private DatabaseReference groupRef;

    public GroupsAdapter(Context context, List<Group> groupsList)
    {
        this.context = context;
        this.groupsList = groupsList;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_display, parent, false);
        GroupsViewHolder viewHolder = new GroupsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position)
    {
        final Group group = groupsList.get(position);
        final String groupID = group.getGid();
        groupRef = FirebaseDatabase.getInstance().getReference("Groups");
        groupRef.child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final String grpName = dataSnapshot.child("name").getValue().toString();
                holder.groupName.setText(grpName);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, GroupActivity.class);
                        intent.putExtra("group_id", groupID);
                        context.startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder
    {
        TextView groupName;
        public GroupsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
        }
    }
}

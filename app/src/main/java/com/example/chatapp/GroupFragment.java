package com.example.chatapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

ArrayList<SingleChat> groups=new ArrayList<>();
RecyclerView groups_list_recycler;
DatabaseReference groupsInfoRef,userInGroupsRef,databaseReference;
 GroupChatRecyclerViewAdapter adapter;
 private String currentUserID;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v=inflater.inflate(R.layout.fragment_group, container, false);
        groups_list_recycler=v.findViewById(R.id.groups_list_recycler);

        initializeVars();

        return v;
    }

    private void initializeVars() {
        databaseReference=FirebaseDatabase.getInstance().getReference();
        groupsInfoRef=databaseReference.child("Groups Info");
        userInGroupsRef=databaseReference.child("User In Groups");
        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupsInfoRef.keepSynced(true);
        adapter=new GroupChatRecyclerViewAdapter(groups,getContext());
        groups_list_recycler.setAdapter(adapter);
        groups_list_recycler.setHasFixedSize(true);
        groups_list_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        setArrayListData();


    }

    private void setArrayListData() {

         //get all the groups where this user is present

        userInGroupsRef.child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String,String> mp=new HashMap<>();


                        for (DataSnapshot gID:dataSnapshot.getChildren())
                        {



                            String groupName=gID.getValue().toString();
//                            Toast.makeText(getContext(),gID.getKey().toString(),Toast.LENGTH_LONG).show();
                            mp.put(groupName,gID.getKey().toString());

                        }



                        groups.clear();

                        Iterator iterator=mp.entrySet().iterator();
                        while (iterator.hasNext())
                        {
                            Map.Entry<String,String> p=(Map.Entry)iterator.next();
                            SingleChat sc=new SingleChat();
                            sc.setGroupName(p.getKey());
                            sc.setGroupID(p.getValue());
                            groups.add(sc);
                            adapter.notifyDataSetChanged();
                            iterator.remove();
                        }


                        showGroups();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(),"Error: "+ databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

        groupsInfoRef.keepSynced(true);

    }

    private void showGroups() {

    }

}

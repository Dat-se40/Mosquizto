package com.example.mosquizto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mosquizto.Activities.ProfilePage;
import com.example.mosquizto.R;
import com.example.mosquizto.Models.Collection;
import com.example.mosquizto.Models.User;
import com.example.mosquizto.Adapters.BasedOnRecentAdapter;
import com.example.mosquizto.Adapters.JumpBackInAdapter;
import com.example.mosquizto.Adapters.RecentAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvJumpBackIn, rvRecents, rvBasedOnRecent;
    private ImageView imgView ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvJumpBackIn = view.findViewById(R.id.rvJumpBackIn);
        rvRecents = view.findViewById(R.id.rvRecents);
        rvBasedOnRecent = view.findViewById(R.id.rvBasedOnRecent);
        imgView = view.findViewById(R.id.imgProfile) ;
        setupJumpBackIn();
        setupRecents();
        setupBasedOnRecent();
        createListener();


        return view;
    }

    private void createListener() {
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfilePage.class);
                startActivity(intent);
            }
        });
    }

    private void setupJumpBackIn() {
        List<Collection> list = new ArrayList<>();
        list.add(new Collection(1, "Unit 5: The World I...", 37, new User("quanghieu"), 1));
        list.add(new Collection(2, "General trivia", 7, new User("Quizlet"), 50));

        JumpBackInAdapter adapter = new JumpBackInAdapter(list);
        rvJumpBackIn.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvJumpBackIn.setAdapter(adapter);
    }

    private void setupRecents() {
        List<Collection> list = new ArrayList<>();
        list.add(new Collection(1, "Unit 5: The World I...", 37, new User("quanghieu"), 0));
        list.add(new Collection(2, "General trivia", 7, new User("Quizlet"), 0));

        RecentAdapter adapter = new RecentAdapter(list);
        rvRecents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvRecents.setAdapter(adapter);
    }

    private void setupBasedOnRecent() {
        List<Collection> list = new ArrayList<>();
        list.add(new Collection(3, "bài 1", 46, new User("lforlinh"), 0));
        list.add(new Collection(4, "bài 2", 45, new User("lforlinh"), 0)); // Thẻ kế tiếp bị che mất trong ảnh

        BasedOnRecentAdapter adapter = new BasedOnRecentAdapter(list);
        rvBasedOnRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBasedOnRecent.setAdapter(adapter);
    }
}
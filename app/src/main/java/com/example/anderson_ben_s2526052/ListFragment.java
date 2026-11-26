package com.example.anderson_ben_s2526052;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private TrafficViewModel vm;
    private SituationAdapter adapter;
    private RadioGroup tabs;
    private TextView emptyText;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);

        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SituationAdapter(vm::select);
        recycler.setAdapter(adapter);

        tabs = view.findViewById(R.id.tabs);
        emptyText = view.findViewById(R.id.emptyText);

        tabs.setOnCheckedChangeListener((group, checkedId) -> updateList());
        vm.incidents().observe(getViewLifecycleOwner(), list -> updateList());
        vm.roadworks().observe(getViewLifecycleOwner(), list -> updateList());
    }

    private void updateList() {
        boolean roadworks = tabs.getCheckedRadioButtonId() == R.id.tabRoadworks;
        List<Situation> list = roadworks ? vm.roadworks().getValue() : vm.incidents().getValue();
        if (list == null) list = new ArrayList<>();
        adapter.setItems(list);
        emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
package com.example.anderson_ben_s2526052;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class DetailFragment extends Fragment {
    private LinearLayout rows;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rows = view.findViewById(R.id.detailRows);
        ImageView icon = view.findViewById(R.id.detailIcon);
        TextView type = view.findViewById(R.id.detailType);
        TextView comment = view.findViewById(R.id.detailComment);
        Button btnMap = view.findViewById(R.id.btnDetailMap);


        TrafficViewModel vm = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        vm.selected().observe(getViewLifecycleOwner(), s -> {
            if (s == null) return;
            icon.setImageResource(UiHelper.iconFor(s));
            type.setText(UiHelper.typeLabel(s));
            comment.setText(s.comment);
            fillRows(s);
            btnMap.setVisibility(s.hasLocation() ? View.VISIBLE : View.GONE);
        });

        btnMap.setOnClickListener(v -> {
            Situation s = vm.selected().getValue();
            if (s != null) {
                vm.showOnMap(Collections.singletonList(s));
                ((MainActivity) requireActivity()).showMap();
            }
        });
    }

    private void fillRows(Situation s) {
        rows.removeAllViews();
        addRow("Category", s.kind == Situation.Kind.ROADWORK ? "Roadworks" : "Unplanned incident");
        addRow("Starts", UiHelper.formatTime(s.startTime));
        addRow("Ends", UiHelper.formatTime(s.endTime));
        addRow("Status", s.validityStatus);
        addRow("Probability", s.probability);
        addRow("Delays", s.delaysType);
        if (s.lanesRestricted != null) addRow("Lanes restricted", String.valueOf(s.lanesRestricted));
        if (s.lanesOperational != null) addRow("Lanes operational", String.valueOf(s.lanesOperational));
        addRow("Compliance", s.compliance);
        if (!s.roads.isEmpty()) addRow("Roads", android.text.TextUtils.join(", ", s.roads));
        if (s.hasLocation()) {
            addRow(s.isLine() ? "From" : "Location", s.fromLat + ", " + s.fromLon);
            if (s.isLine()) addRow("To", s.toLat + ", " + s.toLon);
        }
        addRow("Source", s.source);
    }

    private void addRow(String label, String value) {
        if (value == null || value.isEmpty()) return;      // skip missing data
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_detail_row, rows, false);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.rowValue)).setText(value);
        rows.addView(row);
    }
}
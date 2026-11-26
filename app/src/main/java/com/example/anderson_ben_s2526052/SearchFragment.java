package com.example.anderson_ben_s2526052;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {
    private static final double DEFAULT_LAT = 55.8642;    // Glasgow centre
    private static final double DEFAULT_LON = -4.2518;
    private static final String[] LABELS = {"1 km", "5 km", "10 km", "25 km", "50 km"};
    private static final double[] KM = {1, 5, 10, 25, 50};

    private TrafficViewModel vm;
    private double lat = DEFAULT_LAT, lon = DEFAULT_LON;
    private boolean usingDevice = false;

    private EditText roadInput;
    private RadioGroup typeGroup;
    private Spinner distanceSpinner;
    private TextView locationText, resultsText;
    private SituationAdapter adapter;

    private final ActivityResultLauncher<String> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) useCurrentLocation(); else useDefaultLocation();
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);

        roadInput = view.findViewById(R.id.roadInput);
        typeGroup = view.findViewById(R.id.typeGroup);
        distanceSpinner = view.findViewById(R.id.distanceSpinner);
        locationText = view.findViewById(R.id.locationText);
        resultsText = view.findViewById(R.id.resultsText);

        distanceSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, LABELS));
        distanceSpinner.setSelection(2);                    // 10 km by default

        RecyclerView recycler = view.findViewById(R.id.searchRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SituationAdapter(vm::select);
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnRoadSearch).setOnClickListener(v -> doRoadSearch());
        roadInput.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { doRoadSearch(); return true; }
            return false;
        });
        view.findViewById(R.id.btnMyLocation).setOnClickListener(v -> requestLocation());
        view.findViewById(R.id.btnNearby).setOnClickListener(v -> doNearbySearch());
        view.findViewById(R.id.btnShowOnMap).setOnClickListener(v -> {
            List<Situation> results = vm.searchResults().getValue();
            vm.showOnMap(results == null ? new ArrayList<>() : results);
            ((MainActivity) requireActivity()).showMap();   // opens the map screen
        });

        updateLocationText();

        // results live in the ViewModel, so they survive rotation
        vm.searchResults().observe(getViewLifecycleOwner(), list -> {
            if (list == null) {
                resultsText.setText("Search by road or by distance");
                adapter.setItems(new ArrayList<>());
            } else {
                resultsText.setText(list.isEmpty()
                        ? "No situations found"
                        : list.size() + (list.size() == 1 ? " situation found" : " situations found"));
                adapter.setItems(list);
            }
        });
    }

    private boolean includeIncidents() { return typeGroup.getCheckedRadioButtonId() != R.id.typeRoadworks; }
    private boolean includeRoadworks() { return typeGroup.getCheckedRadioButtonId() != R.id.typeIncidents; }

    private void doRoadSearch() {
        String q = roadInput.getText().toString();
        if (q.trim().isEmpty()) { roadInput.setError("Enter a road name"); return; }
        hideKeyboard();
        vm.searchRoad(q, includeIncidents(), includeRoadworks());
    }

    private void doNearbySearch() {
        hideKeyboard();
        vm.searchNearby(lat, lon, KM[distanceSpinner.getSelectedItemPosition()],
                includeIncidents(), includeRoadworks());
    }

    // ---- location ----
    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            useCurrentLocation();
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void useCurrentLocation() {
//        Location best = null;
//        try {
//            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
//            String[] providers = {
//                    LocationManager.NETWORK_PROVIDER,
//                    LocationManager.GPS_PROVIDER };     // may refuse with approximate permission: ignored
//            for (String p : providers) {
//                try {
//                    Location l = lm.getLastKnownLocation(p);
//                    if (l != null && (best == null || l.getTime() > best.getTime())) best = l;
//                } catch (SecurityException | IllegalArgumentException ignored) { }
//            }
//        } catch (Exception ignored) { }
//
//        if (best != null) {
//            lat = best.getLatitude();
//            lon = best.getLongitude();
//            usingDevice = true;
//            updateLocationText();
//        } else {
//            useDefaultLocation();       // no stored fix: fall back to Glasgow
//        }

        useDefaultLocation();
    }

    private void useDefaultLocation() {
        lat = DEFAULT_LAT;
        lon = DEFAULT_LON;
        usingDevice = false;
        updateLocationText();
    }

    private void updateLocationText() {
        locationText.setText(String.format(Locale.UK, "Searching from: %s (%.4f, %.4f)",
                usingDevice ? "your location" : "Glasgow centre (default)", lat, lon));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = requireActivity().getCurrentFocus();
        if (imm != null && focus != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }
}
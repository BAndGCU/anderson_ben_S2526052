package com.example.anderson_ben_s2526052;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        map.getUiSettings().setZoomControlsEnabled(true);
        TrafficViewModel vm = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        vm.mapItems().observe(getViewLifecycleOwner(), list -> draw(map, list));
    }

    private void draw(GoogleMap map, List<Situation> list) {
        map.clear();
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        int points = 0;
        LatLng last = null;

        for (Situation s : list) {
            if (!s.hasLocation()) continue;
            boolean roadwork = s.kind == Situation.Kind.ROADWORK;

            LatLng from = new LatLng(s.fromLat, s.fromLon);
            map.addMarker(new MarkerOptions()
                    .position(from)
                    .title(UiHelper.typeLabel(s))
                    .snippet(s.comment)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            roadwork ? BitmapDescriptorFactory.HUE_ORANGE
                                    : BitmapDescriptorFactory.HUE_RED)));
            bounds.include(from);
            points++;
            last = from;

            if (s.isLine()) {                       // "from" and "to" define a line
                LatLng to = new LatLng(s.toLat, s.toLon);
                map.addPolyline(new PolylineOptions()
                        .add(from, to)
                        .width(8)
                        .color(roadwork ? Color.parseColor("#EF6C00") : Color.RED));
                bounds.include(to);
                points++;
            }
        }

        if (points == 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(56.5, -4.2), 6f));  // Scotland
        } else if (points == 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(last, 12f));
        } else {
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
            } catch (IllegalStateException e) {      // map not laid out yet
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(last, 8f));
            }
        }
    }
}
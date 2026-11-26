package com.example.anderson_ben_s2526052;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class TrafficViewModel extends AndroidViewModel {
    private final TrafficRepository repo;
    private final MutableLiveData<Situation> selected = new MutableLiveData<>();
    private final MutableLiveData<List<Situation>> mapItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> detailRequested = new MutableLiveData<>(false);

    public TrafficViewModel(@NonNull Application app) {
        super(app);
        repo = new TrafficRepository(app.getFilesDir());
    }

    public LiveData<List<Situation>> incidents() { return repo.getIncidents(); }
    public LiveData<List<Situation>> roadworks() { return repo.getRoadworks(); }
    public LiveData<String> status() { return repo.getStatus(); }
    public LiveData<Situation> selected() { return selected; }
    public LiveData<List<Situation>> mapItems() { return mapItems; }

    public LiveData<Boolean> detailRequested() { return detailRequested; }

    public void select(Situation s) {
        selected.setValue(s);
        detailRequested.setValue(true);      // a one-off request to open the detail screen
    }
    public void detailShown() { detailRequested.setValue(false); }   // called once it has been handled
    public void refresh() { repo.refreshNow(); }
    public void showOnMap(List<Situation> list) { mapItems.setValue(list); }

    private final MutableLiveData<List<Situation>> searchResults = new MutableLiveData<>(null); // null = no search yet

    public LiveData<List<Situation>> searchResults() { return searchResults; }

    private static final Pattern ROAD_CODE = Pattern.compile("^[AM]\\d{1,4}(\\(M\\))?$");

    private List<Situation> sources(boolean inc, boolean rw) {
        List<Situation> all = new ArrayList<>();
        if (inc && incidents().getValue() != null) all.addAll(incidents().getValue());
        if (rw && roadworks().getValue() != null) all.addAll(roadworks().getValue());
        return all;
    }

    // ---- search by road ----
    // Must be called on the UI thread (uses setValue)
    public void searchRoad(String query, boolean inc, boolean rw) {
        String q = query.trim().toUpperCase(Locale.UK).replace(" ", "");
        List<Situation> out = new ArrayList<>();
        if (!q.isEmpty()) {
            boolean isCode = ROAD_CODE.matcher(q).matches();
            for (Situation s : sources(inc, rw)) {
                boolean match;
                if (isCode) {
                    // exact road name, so "A7" does not match "A77"
                    match = false;
                    for (String r : s.roads) if (r.equalsIgnoreCase(q)) { match = true; break; }
                } else {
                    // free text such as a place or bridge name
                    match = s.comment != null
                            && s.comment.toUpperCase(Locale.UK).contains(query.trim().toUpperCase(Locale.UK));
                }
                if (match) out.add(s);
            }
        }
        searchResults.setValue(out);
    }

    // ---- search by distance ----
    // Must be called on the UI thread (uses setValue)
    public void searchNearby(double lat, double lon, double maxKm, boolean inc, boolean rw) {
        List<Situation> out = new ArrayList<>();
        for (Situation s : sources(inc, rw)) {
            if (s.hasLocation() && distanceKm(lat, lon, s) <= maxKm) out.add(s);
        }
        Collections.sort(out, (a, b) -> Float.compare(distanceKm(lat, lon, a), distanceKm(lat, lon, b)));
        searchResults.setValue(out);   // nearest first
    }

    private float distanceKm(double lat, double lon, Situation s) {
        float[] d = new float[1];
        Location.distanceBetween(lat, lon, s.fromLat, s.fromLon, d);
        float best = d[0];
        if (s.isLine()) {                       // a line: use whichever end is closer
            Location.distanceBetween(lat, lon, s.toLat, s.toLon, d);
            best = Math.min(best, d[0]);
        }
        return best / 1000f;
    }
    public void init(String clientId, String clientKey, String unplannedUrl, String roadworksUrl) {
        repo.start(clientId, clientKey, unplannedUrl, roadworksUrl);
    }
    public void showAllOnMap() { mapItems.setValue(sources(true, true)); }
}

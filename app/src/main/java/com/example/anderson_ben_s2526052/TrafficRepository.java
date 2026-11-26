package com.example.anderson_ben_s2526052;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficRepository {
    private static final long REFRESH_MINUTES = 60;     // set to 1 for the video demo

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final MutableLiveData<List<Situation>> incidents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Situation>> roadworks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> status = new MutableLiveData<>("Loading...");
    private final File cacheDir;

    private String clientId, clientKey, unplannedUrl, roadworksUrl;
    private boolean started = false;

    public TrafficRepository(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    /** Safe to call repeatedly (e.g. after rotation): only the first call starts anything. */
    public synchronized void start(String clientId, String clientKey,
                                   String unplannedUrl, String roadworksUrl) {
        if (started) return;
        started = true;
        this.clientId = clientId;
        this.clientKey = clientKey;
        this.unplannedUrl = unplannedUrl;
        this.roadworksUrl = roadworksUrl;
        // runs immediately, then every REFRESH_MINUTES
        executor.scheduleWithFixedDelay(this::refresh, 0, REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    public LiveData<List<Situation>> getIncidents() { return incidents; }
    public LiveData<List<Situation>> getRoadworks() { return roadworks; }
    public LiveData<String> getStatus() { return status; }

    public void refreshNow() { if (started) executor.execute(this::refresh); }

    private void refresh() {
        // One Task per feed; the single-thread executor runs them one after the other
        executor.execute(new Task(unplannedUrl, Situation.Kind.INCIDENT, incidents, "incidents.xml"));
        executor.execute(new Task(roadworksUrl, Situation.Kind.ROADWORK, roadworks, "roadworks.xml"));
    }

    private class Task implements Runnable {
        private String result = "";
        private final String myurl;
        private final Situation.Kind kind;
        private final MutableLiveData<List<Situation>> target;
        private final String cacheName;
        BufferedReader in = null;
        String inputLine = "";

        Task(String aurl, Situation.Kind kind,
             MutableLiveData<List<Situation>> target, String cacheName) {
            this.myurl = aurl;
            this.kind = kind;
            this.target = target;
            this.cacheName = cacheName;
        }

        @Override
        public void run() {
            Log.d("MyTask", "in run: " + kind);
            try {
                download();                      // fills result
                cleanUp();                       // strips leading garbage
                publish(TrafficXmlParser.parse(result, kind));
                writeCache(cacheName, result);
                status.postValue("Updated " + new SimpleDateFormat("HH:mm", Locale.UK).format(new Date()));
            } catch (Exception e) {
                Log.e("MyTask", "failed: " + e);
                useCache();
            }
        }

        private void download() throws IOException {
            URL url = new URL(myurl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            urlConnection.setRequestProperty("Authorization",
                    Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
            try {
                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    throw new IOException("HTTP " + urlConnection.getResponseCode());
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                }
                in.close();
                result = sb.toString();
            } finally {
                urlConnection.disconnect();
            }
        }

        private void cleanUp() throws IOException {
            int i = result.indexOf("<?");        // initial tag
            if (i < 0) throw new IOException("No XML in response");
            result = result.substring(i);
        }

        // Replaces runOnUiThread from starter code
        private void publish(List<Situation> list) {
            target.postValue(list);
        }

        private void useCache() {
            try {
                String cached = readCache(cacheName);
                int i = cached.indexOf("<?");
                publish(TrafficXmlParser.parse(i >= 0 ? cached.substring(i) : cached, kind));
                status.postValue("Offline - showing last saved data");
            } catch (Exception e) {
                status.postValue("No connection and no saved data");
            }
        }
    }
    private void writeCache(String name, String data) {
        try (FileWriter w = new FileWriter(new File(cacheDir, name))) { w.write(data); }
        catch (IOException ignored) { }
    }

    private String readCache(String name) throws IOException {
        File f = new File(cacheDir, name);
        if (!f.exists()) throw new FileNotFoundException(name);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String l; while ((l = r.readLine()) != null) sb.append(l);
        }
        return sb.toString();
    }

}

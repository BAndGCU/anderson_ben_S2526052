package com.example.anderson_ben_s2526052;

import android.graphics.Color;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UiHelper {

    public static int iconFor(Situation s) {
        String t = s.subType == null ? "" : s.subType;
        switch (t) {
            case "roadClosed":     return R.drawable.ic_road_closed;
            case "queuingTraffic": return R.drawable.ic_queue;
            case "roadworks":      return R.drawable.ic_roadworks;
            default: break;
        }
        if (s.lanesRestricted != null) return R.drawable.ic_lane_restricted;
        if (s.kind == Situation.Kind.ROADWORK) return R.drawable.ic_roadworks;
        return R.drawable.ic_warning;
    }

    public static int delayColor(Situation s) {
        if (s.kind != Situation.Kind.ROADWORK) return Color.TRANSPARENT;
        if ("longDelays".equals(s.delaysType)) return Color.parseColor("#FFCDD2"); // red = long
        if ("delays".equals(s.delaysType))     return Color.parseColor("#FFF9C4"); // yellow = short
        return Color.parseColor("#C8E6C9");                                        // green = none
    }

    public static String typeLabel(Situation s) {
        if (s.subType == null) {
            return s.kind == Situation.Kind.ROADWORK ? "Roadworks" : "Incident";
        }
        switch (s.subType) {
            case "roadClosed":     return "Road closed";
            case "queuingTraffic": return "Queuing traffic";
            case "roadworks":      return "Roadworks";
            default:
                // turn e.g. "laneClosures" into "Lane closures"
                String spaced = s.subType.replaceAll("([A-Z])", " $1").toLowerCase(Locale.UK);
                return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
        }
    }

    public static String formatTime(String iso) {
        if (iso == null) return null;
        try {
            return OffsetDateTime.parse(iso)
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK));
        } catch (Exception e) {
            return iso;   // show the raw text if it isn't the expected format
        }
    }
}

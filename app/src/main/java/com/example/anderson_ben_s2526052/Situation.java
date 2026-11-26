package com.example.anderson_ben_s2526052;

import java.util.ArrayList;
import java.util.List;

public class Situation {
    public enum Kind { INCIDENT, ROADWORK }

    public final Kind kind;
    public String id, recordType, comment, source, probability, compliance;
    public String validityStatus, startTime, endTime;
    public String subType;          // roadClosed / queuingTraffic / roadworks ...
    public String delaysType;       // roadworks: "delays" / "longDelays"
    public Integer lanesOperational, lanesRestricted;
    public Double fromLat, fromLon, toLat, toLon;
    public List<String> roads = new ArrayList<>();   // extracted from comment, e.g. "A78"

    public Situation(Kind kind) { this.kind = kind; }

    public boolean hasLocation() { return fromLat != null && fromLon != null; }
    public boolean isLine() { return toLat != null && toLon != null; }
}

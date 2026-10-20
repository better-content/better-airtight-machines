package com.bettercontent.betterairtightmachines.diagnostics;

/** Pure projection of values already observed by the owning native decision. */
public final class FeatureObservation {
    private FeatureObservation() {}
    public static String fields(boolean before, boolean after) { return PlaytestLog.fields("before_sealed", before, "after_sealed", after); }
    
}

package com.bettercontent.betterairtightmachines.diagnostics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class FeatureObservationTest {
    @Test public void nativeScalarsHaveExplicitStage() { var fields = FeatureObservation.fields(false, true); assertTrue(fields.contains("before_sealed=false")); assertTrue(fields.contains("after_sealed=true")); }
    @Test public void uncertaintyAndPrivacyStayExplicit() { var fields = FeatureObservation.fields(true, false); assertTrue(fields.contains("before_sealed=true")); assertTrue(fields.contains("after_sealed=false")); assertFalse(fields.contains("durable")); }
}

/*
 * @test
 * @summary jdk.jfr.FlightRecorder conformance: availability, event types and recordings (reflection fallback if unavailable).
 * @run main FlightRecorderTest
 */

import jdk.jfr.*;

public class FlightRecorderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        boolean avail = FlightRecorder.isAvailable();
        check(avail, "isAvailable");
        if (avail) {
            FlightRecorder fr = FlightRecorder.getFlightRecorder();
            check(fr != null, "getFlightRecorder");
            check(FlightRecorder.isInitialized(), "isInitialized");
            java.util.List<EventType> types = fr.getEventTypes();
            check(types != null && types.size() > 0, "event types non-empty");
            check(fr.getRecordings() != null, "recordings list");
        } else {
            check(FlightRecorder.class.isEnum() == false, "FlightRecorder is a class");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

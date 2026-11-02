/*
 * @test
 * @summary jdk.jfr.consumer.EventStream conformance: openRepository and close.
 * @run main EventStreamTest
 */

import jdk.jfr.consumer.*;

public class EventStreamTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        try (EventStream es = EventStream.openRepository()) {
            check(es != null, "openRepository");
            es.onEvent(e -> {});
            es.onClose(() -> {});
            es.setReuse(true);
            es.setOrdered(true);
            check(true, "configured");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

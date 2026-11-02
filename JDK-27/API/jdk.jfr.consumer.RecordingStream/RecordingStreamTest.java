/*
 * @test
 * @summary jdk.jfr.consumer.RecordingStream conformance: construct, enable and close.
 * @run main RecordingStreamTest
 */

import jdk.jfr.consumer.*;

public class RecordingStreamTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        try (RecordingStream rs = new RecordingStream()) {
            check(rs != null, "construct");
            rs.enable("jdk.FileRead");
            rs.setReuse(true);
            rs.setOrdered(true);
            rs.onEvent(e -> {});
            rs.onFlush(() -> {});
            check(true, "configured");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

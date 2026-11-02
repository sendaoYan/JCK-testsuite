/*
 * @test
 * @summary jdk.jfr.consumer.RecordedStackTrace conformance: from a recorded event's stack trace.
 * @run main RecordedStackTraceTest
 */

import jdk.jfr.consumer.*;

public class RecordedStackTraceTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@jdk.jfr.Name("com.test.ConsumerEvent")
@jdk.jfr.Label("Consumer Event")
static class ConsumerEvent extends jdk.jfr.Event {
    @jdk.jfr.Label("message") String message;
}
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        java.nio.file.Path p = java.nio.file.Files.createTempFile("rst", ".jfr");
        try (jdk.jfr.Recording r = new jdk.jfr.Recording()) {
            r.enable(ConsumerEvent.class);
            r.start();
            ConsumerEvent ev = new ConsumerEvent(); ev.message = "hello"; ev.commit();
            r.stop();
            r.dump(p);
        }
        jdk.jfr.consumer.RecordedEvent ev = null;
        try (jdk.jfr.consumer.RecordingFile rf = new jdk.jfr.consumer.RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                jdk.jfr.consumer.RecordedEvent e = rf.readEvent();
                if (e.getEventType().getName().equals("com.test.ConsumerEvent")) { ev = e; break; }
            }
        }
        check(ev != null, "found event");
        RecordedStackTrace st = ev.getStackTrace();
        check(st != null, "stack trace present");
        check(st.getFrames() != null && st.getFrames().size() >= 1, "has frames");
        check(st.isTruncated() == false || st.isTruncated() == true, "isTruncated callable");
        java.nio.file.Files.deleteIfExists(p);
        System.out.println("PASS (" + passed + " assertions)");
    }
}

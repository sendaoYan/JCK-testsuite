/*
 * @test
 * @summary jdk.jfr.consumer.RecordedEvent conformance: read a custom event and inspect fields.
 * @run main RecordedEventTest
 */

import jdk.jfr.consumer.*;
import java.nio.file.*;

public class RecordedEventTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@jdk.jfr.Name("com.test.ConsumerEvent")
@jdk.jfr.Label("Consumer Event")
static class ConsumerEvent extends jdk.jfr.Event {
    @jdk.jfr.Label("message") String message;
}
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        java.nio.file.Path p = java.nio.file.Files.createTempFile("re", ".jfr");
        try (jdk.jfr.Recording r = new jdk.jfr.Recording()) {
            r.enable(ConsumerEvent.class);
            r.start();
            ConsumerEvent ev = new ConsumerEvent(); ev.message = "hello"; ev.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if (e.getEventType().getName().equals("com.test.ConsumerEvent")) { ev = e; break; }
            }
        }
        check(ev != null, "found custom event");
        check(ev.getEventType() != null, "event type");
        check(ev.getStartTime() != null, "start time");
        check(ev.getDuration() != null, "duration");
        check(ev.hasField("message"), "has message field");
        check("hello".equals(ev.getString("message")), "message value");
        check(ev.getFields() != null, "fields");
        Files.deleteIfExists(p);
        System.out.println("PASS (" + passed + " assertions)");
    }
}

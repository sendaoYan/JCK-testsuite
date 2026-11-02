/*
 * @test
 * @summary jdk.jfr.consumer.RecordedObject conformance: obtained as a RecordedEvent supertype with getValue.
 * @run main RecordedObjectTest
 */

import jdk.jfr.consumer.*;
import java.nio.file.*;

public class RecordedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@jdk.jfr.Name("com.test.ConsumerEvent")
@jdk.jfr.Label("Consumer Event")
static class ConsumerEvent extends jdk.jfr.Event {
    @jdk.jfr.Label("message") String message;
}
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        java.nio.file.Path p = java.nio.file.Files.createTempFile("ro", ".jfr");
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
        check(ev != null, "found event");
        RecordedObject o = ev;
        check(o.hasField("message"), "RecordedObject hasField");
        check("hello".equals(o.getValue("message")), "RecordedObject getValue");
        check(o.getFields() != null, "RecordedObject getFields");
        check(o.toString() != null, "RecordedObject toString");
        Files.deleteIfExists(p);
        System.out.println("PASS (" + passed + " assertions)");
    }
}

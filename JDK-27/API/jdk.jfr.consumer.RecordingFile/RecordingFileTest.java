/*
 * @test
 * @summary jdk.jfr.consumer.RecordingFile conformance: dump a recording and read events back.
 * @run main RecordingFileTest
 */

import jdk.jfr.consumer.*;
import java.nio.file.*;

public class RecordingFileTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@jdk.jfr.Name("com.test.ConsumerEvent")
@jdk.jfr.Label("Consumer Event")
static class ConsumerEvent extends jdk.jfr.Event {
    @jdk.jfr.Label("message") String message;
}
    public static void main(String[] args) throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        java.nio.file.Path p = java.nio.file.Files.createTempFile("rf", ".jfr");
        try (jdk.jfr.Recording r = new jdk.jfr.Recording()) {
            r.enable(ConsumerEvent.class);
            r.start();
            ConsumerEvent ev = new ConsumerEvent(); ev.message = "hello"; ev.commit();
            r.stop();
            r.dump(p);
        }
        check(Files.size(p) > 0, "dump non-empty");
        try (RecordingFile rf = new RecordingFile(p)) {
            check(rf != null, "open");
            int n = 0;
            while (rf.hasMoreEvents()) { RecordedEvent e = rf.readEvent(); if (e != null) n++; }
            check(n > 0, "read events");
        }
        int total = RecordingFile.readAllEvents(p).size();
        check(total > 0, "readAllEvents");
        Files.deleteIfExists(p);
        System.out.println("PASS (" + passed + " assertions)");
    }
}

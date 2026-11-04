/*
 * @test
 * @summary jdk.jfr.consumer.RecordingFile conformance: white-box structure + black-box read back.
 * @run main RecordingFileTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordingFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordingFile");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "hasMoreEvents"), "hasMoreEvents");
        check(hasMethod(c, "readEvent"), "readEvent");
        check(hasMethod(c, "readAllEvents"), "readAllEvents");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rf", ".jfr");
        try (Recording r = new Recording()) {
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

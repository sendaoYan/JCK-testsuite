/*
 * @test
 * @summary jdk.jfr.consumer.RecordedEvent conformance: white-box structure + black-box field read.
 * @run main RecordedEventTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getEventType"), "getEventType");
        check(hasMethod(c, "getStartTime"), "getStartTime");
        check(hasMethod(c, "getDuration"), "getDuration");
        check(hasMethod(c, "getString"), "getString");
        check(hasMethod(c, "hasField"), "hasField");
        check(hasMethod(c, "getStackTrace"), "getStackTrace");
        check(hasMethod(c, "getThread"), "getThread");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("re", ".jfr");
        try (Recording r = new Recording()) {
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
                if ("com.test.ConsumerEvent".equals(e.getEventType().getName())) { ev = e; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "found custom event");
        check(ev.getEventType() != null, "event type");
        check(ev.getStartTime() != null, "start time");
        check(ev.getDuration() != null, "duration");
        check(ev.hasField("message"), "has message field");
        check("hello".equals(ev.getString("message")), "message value");
        check(ev.getFields() != null, "fields");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

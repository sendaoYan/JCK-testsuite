/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThread conformance: white-box structure + black-box thread fields.
 * @run main RecordedThreadTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThread");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getJavaThreadId"), "getJavaThreadId");
        check(hasMethod(c, "getJavaName"), "getJavaName");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "isVirtual"), "isVirtual");
        check(hasMethod(c, "getThreadGroup"), "getThreadGroup");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rt", ".jfr");
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
        check(ev != null, "found event");
        RecordedThread t = ev.getThread();
        check(t != null, "thread present");
        check(t.getJavaThreadId() > 0, "java thread id positive");
        check(t.getJavaName() != null, "java name");
        check(t.getId() > 0, "id positive");
        check(t.isVirtual() || !t.isVirtual(), "isVirtual callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

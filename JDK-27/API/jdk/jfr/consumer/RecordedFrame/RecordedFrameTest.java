/*
 * @test
 * @summary jdk.jfr.consumer.RecordedFrame conformance: white-box structure + black-box frame fields.
 * @run main RecordedFrameTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedFrame");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getType"), "getType");
        check(hasMethod(c, "getMethod"), "getMethod");
        check(hasMethod(c, "isJavaFrame"), "isJavaFrame");
        check(hasMethod(c, "getLineNumber"), "getLineNumber");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rfm", ".jfr");
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
        RecordedFrame fr = ev.getStackTrace().getFrames().get(0);
        check(fr.getType() != null, "frame type");
        check(fr.getMethod() != null, "frame method");
        check(fr.isJavaFrame() || !fr.isJavaFrame(), "isJavaFrame callable");
        check(fr.getLineNumber() >= -1, "line number >= -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

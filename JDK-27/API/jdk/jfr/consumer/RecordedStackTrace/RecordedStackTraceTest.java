/*
 * @test
 * @summary jdk.jfr.consumer.RecordedStackTrace conformance: white-box structure + black-box frames.
 * @run main RecordedStackTraceTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedStackTraceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    @Name("test.RecordedStackTrace.NoStack")
    static class NoStack extends Event {
        @Label("v") long v;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedStackTrace");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getFrames"), "getFrames");
        check(hasMethod(c, "isTruncated"), "isTruncated");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rst", ".jfr");
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
        RecordedStackTrace st = ev.getStackTrace();
        check(st != null, "stack trace present");
        check(st.getFrames() != null && st.getFrames().size() >= 1, "has frames");
        check(st.isTruncated() || !st.isTruncated(), "isTruncated callable");
    }

    // ---- deepening: with/without stack trace settings matrix ----
    static void testSettingMatrix() throws Exception {
        Path p = Files.createTempFile("rst2", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(ConsumerEvent.class).withStackTrace();
            r.enable(NoStack.class).withoutStackTrace();
            r.start();
            ConsumerEvent with = new ConsumerEvent(); with.message = "with"; with.commit();
            NoStack without = new NoStack(); without.v = 1; without.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent with = null, without = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if ("com.test.ConsumerEvent".equals(e.getEventType().getName())) with = e;
                if ("test.RecordedStackTrace.NoStack".equals(e.getEventType().getName())) without = e;
            }
        }
        Files.deleteIfExists(p);
        check(with != null && without != null, "both fixtures found");
        // withStackTrace -> recorded and complete enough to reach main
        RecordedStackTrace st = with.getStackTrace();
        check(st != null, "withStackTrace -> non-null");
        check(st.getFrames().size() >= 1, "withStackTrace -> frames");
        check(!st.isTruncated(), "small stack not truncated");
        boolean sawMain = false;
        for (RecordedFrame f : st.getFrames()) if ("main".equals(f.getMethod().getName())) sawMain = true;
        check(sawMain, "withStackTrace reaches main frame");
        check(st.toString() != null, "stack trace toString non-null");
        // withoutStackTrace -> absent entirely
        check(without.getStackTrace() == null, "withoutStackTrace -> null");
        check(without.getLong("v") == 1L, "no-stack event payload intact");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testSettingMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.consumer.RecordedFrame conformance: white-box structure + black-box frame fields.
 * @run main RecordedFrameTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

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
        check(hasMethod(c, "getBytecodeIndex"), "getBytecodeIndex");
    }

    static RecordedEvent recordOne() throws Exception {
        Path p = Files.createTempFile("rfm", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(ConsumerEvent.class).withStackTrace();
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
        return ev;
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        RecordedEvent ev = recordOne();
        check(ev != null, "found event");
        RecordedFrame fr = ev.getStackTrace().getFrames().get(0);
        check(fr.getType() != null, "frame type");
        check(fr.getMethod() != null, "frame method");
        check(fr.isJavaFrame() || !fr.isJavaFrame(), "isJavaFrame callable");
        check(fr.getLineNumber() >= -1, "line number >= -1");
    }

    // ---- deepening: frame attribute matrix on the committing frame ----
    static boolean declaresMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testFrameMatrix() throws Exception {
        RecordedEvent ev = recordOne();
        check(ev != null, "deep fixture event");
        List<RecordedFrame> frames = ev.getStackTrace().getFrames();
        check(frames != null && !frames.isEmpty(), "frames non-empty");
        RecordedFrame fr = frames.get(0);
        check(fr.isJavaFrame(), "committing frame is a Java frame");
        check("RecordedFrameTest".equals(fr.getMethod().getType().getName()), "frame0 declaring class is the test class");
        check(declaresMethod(RecordedFrameTest.class, fr.getMethod().getName()),
              "frame0 method is a method of the test class (was " + fr.getMethod().getName() + ")");
        check(fr.getBytecodeIndex() >= 0, "bytecode index >= 0");
        check(fr.getLineNumber() >= 0, "committing frame has a real line number");
        String type = fr.getType();
        check(type != null, "frame type non-null");
        check("Interpreted".equals(type) || "JIT compiled".equals(type) || "Inlined".equals(type),
              "frame type in {Interpreted, JIT compiled, Inlined} (was " + type + ")");
        // every frame in a Java stack walk satisfies the same invariants
        for (RecordedFrame f : frames) {
            check(f.getMethod() != null, "each frame has a method");
            check(f.getBytecodeIndex() >= -1, "each frame bci >= -1");
            check(f.getLineNumber() >= -1, "each frame line >= -1");
        }
        // walking up the stack must eventually reach main (jtreg agent frames may sit above)
        boolean sawMain = false;
        for (RecordedFrame f : frames) {
            if ("main".equals(f.getMethod().getName()) && "RecordedFrameTest".equals(f.getMethod().getType().getName())) sawMain = true;
        }
        check(sawMain, "stack contains test main frame");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testFrameMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

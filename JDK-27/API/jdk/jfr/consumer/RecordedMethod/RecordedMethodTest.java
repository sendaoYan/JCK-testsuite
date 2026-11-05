/*
 * @test
 * @summary jdk.jfr.consumer.RecordedMethod conformance: white-box structure + black-box method fields.
 * @run main RecordedMethodTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedMethod");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescriptor"), "getDescriptor");
        check(hasMethod(c, "getType"), "getType");
        check(hasMethod(c, "isHidden"), "isHidden");
        check(hasMethod(c, "getModifiers"), "getModifiers");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rmd", ".jfr");
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
        RecordedMethod m = ev.getStackTrace().getFrames().get(0).getMethod();
        check(m != null, "method present");
        check(m.getName() != null, "method name");
        check(m.getDescriptor() != null, "descriptor");
        check(m.getType() != null, "declaring class");
        check(m.isHidden() || !m.isHidden(), "isHidden callable");
    }

    // ---- deepening: precise method identity from the committing frame ----
    static void testMethodIdentity() throws Exception {
        Path p = Files.createTempFile("rmd2", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(ConsumerEvent.class).withStackTrace();
            r.start();
            ConsumerEvent ev = new ConsumerEvent(); ev.message = "deep"; ev.commit();
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
        check(ev != null, "deep fixture event");
        RecordedMethod m = ev.getStackTrace().getFrames().get(0).getMethod();
        // the committing frame is the test-class method that called commit()
        check("RecordedMethodTest".equals(m.getType().getName()), "declaring class == test class");
        boolean declared = false;
        for (Method dm : RecordedMethodTest.class.getDeclaredMethods()) if (dm.getName().equals(m.getName())) declared = true;
        check(declared, "committing method is declared by the test class (was " + m.getName() + ")");
        check(m.getDescriptor() != null && m.getDescriptor().startsWith("("), "descriptor is a JVM descriptor");
        check(m.getModifiers() != 0, "method modifiers non-zero");
        check(!m.isHidden(), "test frame is not hidden");
        check(m.toString() != null, "toString non-null");
        // RecordedMethod extends RecordedObject: field names visible
        check(m.hasField("name") || m.hasField("type"), "method object exposes its fields");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testMethodIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

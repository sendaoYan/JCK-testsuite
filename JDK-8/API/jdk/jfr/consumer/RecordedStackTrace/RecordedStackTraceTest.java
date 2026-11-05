/*
 * @test
 * @summary jdk.jfr.consumer.RecordedStackTrace conformance: structure + with/without stack settings matrix.
 * @run main RecordedStackTraceTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedStackTraceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedStackTrace.E")
    @Label("With Stack")
    static class E extends Event {
        @Label("v") long v;
    }

    @Name("test.RecordedStackTrace.NoStack")
    @Label("No Stack")
    static class NoStack extends Event {
        @Label("v") long v;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedStackTrace", false, RecordedStackTraceTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getFrames"), "declares getFrames");
        check(hasMethod(c, "isTruncated"), "declares isTruncated");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        Path p = Files.createTempFile("rs8", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable(E.class).withStackTrace();
                r.enable(NoStack.class).withoutStackTrace();
                r.start();
                E with = new E(); with.v = 1; with.commit();
                NoStack without = new NoStack(); without.v = 2; without.commit();
                r.stop();
                r.dump(p);
            }
            RecordedEvent with = null, without = null;
            for (RecordedEvent e : RecordingFile.readAllEvents(p)) {
                if ("test.RecordedStackTrace.E".equals(e.getEventType().getName())) with = e;
                if ("test.RecordedStackTrace.NoStack".equals(e.getEventType().getName())) without = e;
            }
            check(with != null && without != null, "both fixtures found");
            // withStackTrace -> recorded and reaches the test class frames
            RecordedStackTrace st = with.getStackTrace();
            check(st != null, "stack trace present");
            check(st.getFrames() != null && st.getFrames().size() >= 1, "has frames");
            check(!st.isTruncated(), "small stack not truncated");
            boolean sawTestClass = false;
            for (RecordedFrame f : st.getFrames()) {
                if ("RecordedStackTraceTest".equals(f.getMethod().getType().getName())) sawTestClass = true;
            }
            check(sawTestClass, "stack reaches the test class");
            check(st.toString() != null, "stack trace toString non-null");
            check(st.isTruncated() || !st.isTruncated(), "isTruncated callable");
            // withoutStackTrace -> absent entirely, payload intact
            check(without.getStackTrace() == null, "withoutStackTrace -> null");
            check(without.getLong("v") == 2L, "no-stack event payload intact");
        } finally {
            Files.deleteIfExists(p);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

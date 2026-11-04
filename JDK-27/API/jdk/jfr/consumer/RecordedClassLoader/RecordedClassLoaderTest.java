/*
 * @test
 * @summary jdk.jfr.consumer.RecordedClassLoader conformance: white-box structure + black-box from recorded class.
 * @run main RecordedClassLoaderTest
 */

import java.lang.reflect.*;
import java.nio.file.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.CLEvent")
    static class CLEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedClassLoader");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == RecordedObject.class, "extends RecordedObject");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "getType"), "getType");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable, skip"); return; }
        Path p = Files.createTempFile("rcl", ".jfr");
        RecordedEvent ev = null;
        try (Recording r = new Recording()) {
            r.enable(CLEvent.class);
            r.start();
            CLEvent e = new CLEvent(); e.message = "hello"; e.commit();
            r.stop();
            r.dump(p);
        }
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent x = rf.readEvent();
                if ("com.test.CLEvent".equals(x.getEventType().getName())) { ev = x; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "found event");
        RecordedClass cls = ev.getStackTrace().getFrames().get(0).getMethod().getType();
        RecordedClassLoader cl = cls.getClassLoader();
        check(cl == null || cl.getName() != null, "class loader name callable when present");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

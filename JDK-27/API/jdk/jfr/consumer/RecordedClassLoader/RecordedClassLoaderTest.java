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

    @Name("test.RecordedClassLoader.E")
    static class E extends Event {
        @Label("cls") Class<?> cls;
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

    // ---- deepening: full loader attribute round-trip on JDK-27 ----
    static void testLoaderAttributes() throws Exception {
        Path p = Files.createTempFile("rcl2", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.cls = RecordedClassLoaderTest.class; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent x = rf.readEvent();
                if ("test.RecordedClassLoader.E".equals(x.getEventType().getName())) { ev = x; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "fixture event");
        RecordedClassLoader cl = ev.getClass("cls").getClassLoader();
        check(cl != null, "app loader recorded");
        check("app".equals(cl.getName()), "getName == \"app\" (JDK-27 app loader name)");
        check(cl.getId() > 0, "loader id > 0");
        RecordedClass type = cl.getType();
        check(type != null, "getType non-null");
        check("jdk.internal.loader.ClassLoaders$AppClassLoader".equals(type.getName()), "type name is AppClassLoader binary name");
        // RecordedClassLoader is a RecordedObject: field channel also works
        check(cl.hasField("name") || cl.hasField("type"), "loader object exposes fields");
        check(cl.toString() != null, "loader toString non-null");
        // stable identity: re-recording yields the same loader id
        Path p2 = Files.createTempFile("rcl3", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            E e = new E(); e.cls = RecordedClassLoaderTest.class; e.commit();
            r.stop();
            r.dump(p2);
        }
        RecordedEvent ev2 = null;
        for (RecordedEvent x : RecordingFile.readAllEvents(p2)) {
            if ("test.RecordedClassLoader.E".equals(x.getEventType().getName())) { ev2 = x; break; }
        }
        Files.deleteIfExists(p2);
        check(ev2 != null, "second fixture event");
        RecordedClassLoader cl2 = ev2.getClass("cls").getClassLoader();
        check(cl2.getId() == cl.getId(), "loader id stable across recordings");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testLoaderAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

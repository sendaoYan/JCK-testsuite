/*
 * @test
 * @summary jdk.jfr.consumer.RecordedClass conformance: white-box structure + black-box class fields.
 * @run main RecordedClassTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    @Name("test.RecordedClass.E")
    @Label("Recorded Class Event")
    static class E extends Event {
        @Label("cls") Class<?> cls;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedClass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "getModifiers"), "getModifiers");
        check(hasMethod(c, "getClassLoader"), "getClassLoader");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rcl", ".jfr");
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
        RecordedClass c = ev.getStackTrace().getFrames().get(0).getMethod().getType();
        check(c != null, "class present");
        check(c.getName() != null, "class name");
        check(c.getId() > 0, "class id");
        check(c.getModifiers() >= 0, "modifiers non-negative");
    }

    // ---- deepening: explicit Class field round-trip through serialization ----
    static void testClassField() throws Exception {
        Path p = Files.createTempFile("rcl2", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.cls = RecordedClassTest.class; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if ("test.RecordedClass.E".equals(e.getEventType().getName())) { ev = e; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "fixture event");
        RecordedClass rc = ev.getClass("cls");
        check(rc != null, "getClass(String) round-trip");
        check("RecordedClassTest".equals(rc.getName()), "recorded class name == test class name");
        check(rc.getId() > 0, "recorded class id > 0");
        check(rc.getModifiers() != 0, "recorded class modifiers non-zero (public)");
        RecordedClassLoader loader = rc.getClassLoader();
        check(loader != null, "app class loader recorded (non-bootstrap)");
        check("app".equals(loader.getName()), "loader name == \"app\" on JDK-27");
        check(loader.getId() > 0, "loader id > 0");
        // type channel: the loader's own class is itself recordable
        RecordedClass loaderType = loader.getType();
        check(loaderType != null && loaderType.getName().contains("AppClassLoader"), "loader type is the AppClassLoader class");
        // frame channel agrees: stack frame's declaring class is the same test class
        RecordedClass frameClass = ev.getStackTrace().getFrames().get(0).getMethod().getType();
        check("RecordedClassTest".equals(frameClass.getName()), "frame declaring class == test class");
        check(frameClass.getId() == rc.getId(), "same class id across fields and frames");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testClassField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

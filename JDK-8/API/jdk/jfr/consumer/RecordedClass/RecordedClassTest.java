/*
 * @test
 * @summary jdk.jfr.consumer.RecordedClass conformance: structure + Class field round-trip.
 * @run main RecordedClassTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedClass.E")
    @Label("Recorded Class Event")
    static class E extends Event {
        @Label("cls") Class<?> cls;
    }

    static RecordedEvent recordOne() throws Exception {
        Path p = Files.createTempFile("rc8", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.cls = RecordedClassTest.class; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        for (RecordedEvent e : RecordingFile.readAllEvents(p)) {
            if ("test.RecordedClass.E".equals(e.getEventType().getName())) { ev = e; break; }
        }
        Files.deleteIfExists(p);
        return ev;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedClass", false, RecordedClassTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getClassLoader"), "declares getClassLoader");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getModifiers"), "declares getModifiers");
        check(hasMethod(c, "getName"), "declares getName");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        RecordedEvent ev = recordOne();
        check(ev != null, "fixture event");
        RecordedClass rc = ev.getClass("cls");
        check(rc != null, "getClass(String) round-trip");
        check("RecordedClassTest".equals(rc.getName()), "recorded class name == test class name");
        check(rc.getId() > 0, "recorded class id > 0");
        check(rc.getModifiers() != 0, "recorded class modifiers non-zero");
        RecordedClassLoader loader = rc.getClassLoader();
        check(loader != null, "app class loader recorded (non-bootstrap)");
        // JDK-8 quirk: RecordedClassLoader.getName() reports the internal slashed name
        check(loader.getName() != null && loader.getName().contains("AppClassLoader"), "loader name contains AppClassLoader");
        check(loader.getId() > 0, "loader id > 0");
        RecordedClass loaderType = loader.getType();
        check(loaderType != null && loaderType.getName().contains("AppClassLoader"), "loader type is the AppClassLoader class");
        // frame channel agrees: stack frame's declaring class is the same test class
        RecordedClass frameClass = ev.getStackTrace().getFrames().get(0).getMethod().getType();
        check("RecordedClassTest".equals(frameClass.getName()), "frame declaring class == test class");
        check(frameClass.getId() == rc.getId(), "same class id across fields and frames");
        check(rc.toString() != null, "toString non-null");
        // mismatch: getClass on non-Class field
        try { ev.getClass("id"); check(false, "getClass on absent field should throw"); }
        catch (IllegalArgumentException expected) { check(true, "getClass(missing) -> IAE"); }
        check(!ev.hasField("nope"), "hasField unknown false");
        check(ev.getFields() != null && !ev.getFields().isEmpty(), "getFields non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

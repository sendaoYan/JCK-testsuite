/*
 * @test
 * @summary jdk.jfr.consumer.RecordedClassLoader conformance: structure + loader attribute round-trip.
 * @run main RecordedClassLoaderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedClassLoader.E")
    static class E extends Event {
        @Label("cls") Class<?> cls;
    }

    static RecordedEvent recordOne() throws Exception {
        Path p = Files.createTempFile("rcl8", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.cls = RecordedClassLoaderTest.class; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        for (RecordedEvent x : RecordingFile.readAllEvents(p)) {
            if ("test.RecordedClassLoader.E".equals(x.getEventType().getName())) { ev = x; break; }
        }
        Files.deleteIfExists(p);
        return ev;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedClassLoader", false, RecordedClassLoaderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getType"), "declares getType");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        RecordedEvent ev = recordOne();
        check(ev != null, "fixture event");
        RecordedClassLoader cl = ev.getClass("cls").getClassLoader();
        check(cl != null, "app loader recorded");
        // JDK-8: getName() reports the slashed internal name of the app class loader
        check("sun/misc/Launcher$AppClassLoader".equals(cl.getName()), "getName == slashed AppClassLoader name (JDK-8 form)");
        check(cl.getId() > 0, "loader id > 0");
        RecordedClass type = cl.getType();
        check(type != null, "getType non-null");
        check("sun.misc.Launcher$AppClassLoader".equals(type.getName()), "type name is AppClassLoader binary name (dotted)");
        check(cl.toString() != null, "toString non-null");
        // stable identity: re-recording yields the same loader id
        RecordedEvent ev2 = recordOne();
        check(ev2 != null, "second fixture event");
        RecordedClassLoader cl2 = ev2.getClass("cls").getClassLoader();
        check(cl2.getId() == cl.getId(), "loader id stable across recordings");
        // frame channel: the frame's declaring class is loaded by the same loader
        RecordedClassLoader frameLoader = ev.getStackTrace().getFrames().get(0).getMethod().getType().getClassLoader();
        check(frameLoader.getId() == cl.getId(), "frame class loader id == field class loader id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

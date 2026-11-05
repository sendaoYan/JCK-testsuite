/*
 * @test
 * @summary jdk.jfr.consumer.RecordedMethod conformance: structure + method identity from stack frames.
 * @run main RecordedMethodTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedMethod.E")
    @Label("Recorded Method Event")
    static class E extends Event {
        @Label("v") long v;
    }

    static RecordedEvent recordOne() throws Exception {
        Path p = Files.createTempFile("rm8", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.v = 1; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        for (RecordedEvent x : RecordingFile.readAllEvents(p)) {
            if ("test.RecordedMethod.E".equals(x.getEventType().getName())) { ev = x; break; }
        }
        Files.deleteIfExists(p);
        return ev;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedMethod", false, RecordedMethodTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getDescriptor"), "declares getDescriptor");
        check(hasMethod(c, "getModifiers"), "declares getModifiers");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "isHidden"), "declares isHidden");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        RecordedEvent ev = recordOne();
        check(ev != null, "fixture event");
        RecordedMethod m = ev.getStackTrace().getFrames().get(0).getMethod();
        check(m != null, "method present");
        // the committing frame is the test-class method that called commit()
        check("RecordedMethodTest".equals(m.getType().getName()), "declaring class == test class");
        boolean declared = false;
        for (Method dm : RecordedMethodTest.class.getDeclaredMethods()) if (dm.getName().equals(m.getName())) declared = true;
        check(declared, "committing method is declared by the test class (was " + m.getName() + ")");
        check(m.getDescriptor() != null && m.getDescriptor().startsWith("("), "descriptor is a JVM descriptor");
        check(m.getType() != null, "declaring class");
        check(m.getModifiers() != 0, "method modifiers non-zero");
        check(!m.isHidden(), "test frame is not hidden");
        check(m.getName() != null, "method name");
        check(m.toString() != null, "toString non-null");
        check(m.isHidden() || !m.isHidden(), "isHidden callable");
        // every frame method is fully populated
        for (RecordedFrame f : ev.getStackTrace().getFrames()) {
            check(f.getMethod() != null && f.getMethod().getName() != null, "each frame has a named method");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

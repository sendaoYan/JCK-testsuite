/*
 * @test
 * @summary jdk.jfr.consumer.RecordedFrame conformance: structure + frame attribute matrix.
 * @run main RecordedFrameTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedFrame.E")
    @Label("Recorded Frame Event")
    static class E extends Event {
        @Label("v") long v;
    }

    static RecordedEvent recordOne() throws Exception {
        Path p = Files.createTempFile("rf8", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E(); e.v = 1; e.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        for (RecordedEvent x : RecordingFile.readAllEvents(p)) {
            if ("test.RecordedFrame.E".equals(x.getEventType().getName())) { ev = x; break; }
        }
        Files.deleteIfExists(p);
        return ev;
    }

    static boolean declaresMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedFrame", false, RecordedFrameTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getBytecodeIndex"), "declares getBytecodeIndex");
        check(hasMethod(c, "getLineNumber"), "declares getLineNumber");
        check(hasMethod(c, "getMethod"), "declares getMethod");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "isJavaFrame"), "declares isJavaFrame");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        RecordedEvent ev = recordOne();
        check(ev != null, "found event");
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
        // walking up the stack must eventually reach the test's main frame
        boolean sawMain = false;
        for (RecordedFrame f : frames) {
            if ("main".equals(f.getMethod().getName()) && "RecordedFrameTest".equals(f.getMethod().getType().getName())) sawMain = true;
        }
        check(sawMain, "stack contains test main frame");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThreadGroup conformance: structure + group hierarchy via jdk.ThreadStart.
 * @run main RecordedThreadGroupTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedThreadGroupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("com.test.ConsumerEvent8")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThreadGroup", false, RecordedThreadGroupTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getParent"), "declares getParent");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        String creatorGroup = Thread.currentThread().getThreadGroup().getName();
        Path p = Files.createTempFile("rtg8", ".jfr");
        Thread named = new Thread(() -> {}, "rtg8-named-thread");
        try {
            try (Recording r = new Recording()) {
                r.enable("jdk.ThreadStart");
                r.enable(ConsumerEvent.class);
                r.start();
                named.start();
                named.join(3000);
                ConsumerEvent ev = new ConsumerEvent(); ev.message = "grp"; ev.commit();
                r.stop();
                r.dump(p);
            }
            // find the ThreadStart of our named thread (other system threads may also start)
            RecordedEvent start = null;
            for (RecordedEvent e : RecordingFile.readAllEvents(p)) {
                if (!"jdk.ThreadStart".equals(e.getEventType().getName())) continue;
                if (e.getThread() != null && "rtg8-named-thread".equals(e.getThread().getJavaName())) { start = e; break; }
            }
            check(start != null, "jdk.ThreadStart captured for the named thread");
            RecordedThreadGroup g = start.getThread().getThreadGroup();
            check(g != null, "started thread has a group");
            check(creatorGroup.equals(g.getName()),
                  "thread inherits the creating thread's group (expected " + creatorGroup + ", was " + g.getName() + ")");
            // walking parents must reach the system root group and terminate
            RecordedThreadGroup cur = g;
            boolean sawSystem = false;
            for (int hop = 0; hop < 6 && cur != null; hop++) {
                if ("system".equals(cur.getName())) sawSystem = true;
                cur = cur.getParent();
            }
            check(sawSystem, "group chain contains system");
            check(cur == null, "group chain terminates at root (null parent)");
            check(g.toString() != null, "group toString non-null");
            // implicit channel: committing thread's group
            RecordedEvent ce = null;
            for (RecordedEvent e : RecordingFile.readAllEvents(p)) {
                if ("com.test.ConsumerEvent8".equals(e.getEventType().getName())) { ce = e; break; }
            }
            check(ce != null, "implicit-channel fixture");
            RecordedThreadGroup mg = ce.getThread().getThreadGroup();
            check(mg != null && creatorGroup.equals(mg.getName()), "committing thread's group == creating thread's group");
        } finally {
            Files.deleteIfExists(p);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

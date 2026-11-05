/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThread conformance: structure + recorded thread attribute contract.
 * @run main RecordedThreadTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

    @Name("test.RecordedThread.E")
    @Label("Recorded Thread Event")
    static class E extends Event {
        @Label("thr") Thread thr;
        @Label("other") Thread other;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThread", false, RecordedThreadTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getId"), "declares getId");
        check(hasMethod(c, "getJavaName"), "declares getJavaName");
        check(hasMethod(c, "getJavaThreadId"), "declares getJavaThreadId");
        check(hasMethod(c, "getOSName"), "declares getOSName");
        check(hasMethod(c, "getOSThreadId"), "declares getOSThreadId");
        check(hasMethod(c, "getThreadGroup"), "declares getThreadGroup");
        check(!hasMethod(c, "isVirtual"), "no isVirtual() on JDK-8 (added in JDK 21)");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        Path file = Files.createTempFile("rt8", ".jfr");
        try {
            final java.util.concurrent.CountDownLatch hold = new java.util.concurrent.CountDownLatch(1);
            Thread named = new Thread(() -> {
                try { hold.await(); } catch (InterruptedException ignored) {}
            }, "rt8-named-thread");
            try (Recording r = new Recording()) {
                r.enable(E.class);
                r.start();
                named.start();
                // commit while the named thread is still alive so the field reference resolves
                E onCurrent = new E(); onCurrent.thr = Thread.currentThread(); onCurrent.other = named; onCurrent.commit();
                hold.countDown();
                named.join(3000);
                r.stop();
                r.dump(file);
            }
            RecordedEvent ev = null;
            for (RecordedEvent e : RecordingFile.readAllEvents(file)) {
                if ("test.RecordedThread.E".equals(e.getEventType().getName())) { ev = e; break; }
            }
            check(ev != null, "fixture event found");
            String creatorGroup = Thread.currentThread().getThreadGroup().getName();
            // current (committing) thread recorded through an explicit field
            RecordedThread cur = ev.getThread("thr");
            check(cur != null, "getThread(\"thr\")");
            check(Thread.currentThread().getName().equals(cur.getJavaName()), "javaName == current thread");
            check(cur.getJavaThreadId() > 0, "javaThreadId > 0");
            check(cur.getJavaThreadId() == Thread.currentThread().getId(), "javaThreadId == Thread.currentThread().getId()");
            // JDK-8: getId() is a JFR-internal id that need not equal getJavaThreadId()
            check(cur.getId() > 0, "id > 0");
            check(cur.getOSThreadId() > 0, "osThreadId > 0");
            check(cur.getOSName() != null && cur.getOSName().equals(cur.getJavaName()), "osName == javaName");
            check(cur.getThreadGroup() != null && creatorGroup.equals(cur.getThreadGroup().getName()),
                  "group == creating thread's group (was " + cur.getThreadGroup().getName() + ")");
            // a second, distinct thread recorded via another field keeps its own identity
            RecordedThread other = ev.getThread("other");
            check(other != null, "getThread(\"other\")");
            check("rt8-named-thread".equals(other.getJavaName()), "named thread javaName");
            check(other.getJavaThreadId() != cur.getJavaThreadId(), "two distinct threads have distinct java ids");
            check(other.getOSName() != null && other.getOSName().equals(other.getJavaName()), "named thread osName == javaName");
            check(other.getOSThreadId() > 0, "named thread osThreadId > 0 (committed while alive)");
            check(other.getThreadGroup() != null && creatorGroup.equals(other.getThreadGroup().getName()), "named thread group == creator group");
            check(other.toString() != null, "toString non-null");
            // implicit eventThread channel equals the committing thread
            check(ev.getThread().getJavaName().equals(cur.getJavaName()), "implicit getThread() == committing thread");
        } finally {
            Files.deleteIfExists(file);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

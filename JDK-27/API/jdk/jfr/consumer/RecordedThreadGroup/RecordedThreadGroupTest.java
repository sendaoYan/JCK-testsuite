/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThreadGroup conformance: white-box structure + black-box name.
 * @run main RecordedThreadGroupTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordedThreadGroupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThreadGroup");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getParent"), "getParent");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rtg", ".jfr");
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
        RecordedThreadGroup g = ev.getThread().getThreadGroup();
        check(g == null || g.getName() != null, "thread group name callable when present");
    }

    // ---- deepening: real group hierarchy main -> system -> null via jdk.ThreadStart ----
    static void testGroupHierarchy() throws Exception {
        Path p = Files.createTempFile("rtg2", ".jfr");
        Thread named = new Thread(() -> {}, "rtg-named-thread");
        try (Recording r = new Recording()) {
            r.enable("jdk.ThreadStart");
            r.start();
            named.start();
            named.join(3000);
            r.stop();
            r.dump(p);
        }
        RecordedEvent start = null;
        for (RecordedEvent e : RecordingFile.readAllEvents(p)) {
            if (!"jdk.ThreadStart".equals(e.getEventType().getName())) continue;
            if (e.getThread() != null && "rtg-named-thread".equals(e.getThread().getJavaName())) { start = e; break; }
        }
        Files.deleteIfExists(p);
        check(start != null, "jdk.ThreadStart captured for the named thread");
        String creatorGroup = Thread.currentThread().getThreadGroup().getName();
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
        // group of the committing thread (implicit eventThread) is also main
        Path p2 = Files.createTempFile("rtg3", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(ConsumerEvent.class);
            r.start();
            ConsumerEvent ev = new ConsumerEvent(); ev.message = "grp"; ev.commit();
            r.stop();
            r.dump(p2);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p2)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if ("com.test.ConsumerEvent".equals(e.getEventType().getName())) { ev = e; break; }
            }
        }
        Files.deleteIfExists(p2);
        check(ev != null, "implicit-channel fixture");
        RecordedThreadGroup mg = ev.getThread().getThreadGroup();
        check(mg != null && creatorGroup.equals(mg.getName()), "committing thread's group == creating thread's group");
        check(mg.toString() != null, "group toString non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testGroupHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

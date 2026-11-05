/*
 * @test
 * @summary jdk.jfr.consumer.RecordedThread conformance: white-box structure + black-box recorded threads.
 * @run main RecordedThreadTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

public class RecordedThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    @Name("test.RecordedThread.E")
    @Label("Recorded Thread Event")
    static class E extends Event {
        @Label("thr") Thread thr;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedThread");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getJavaThreadId"), "getJavaThreadId");
        check(hasMethod(c, "getJavaName"), "getJavaName");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "isVirtual"), "isVirtual");
        check(hasMethod(c, "getThreadGroup"), "getThreadGroup");
        check(hasMethod(c, "getOSName"), "getOSName");
        check(hasMethod(c, "getOSThreadId"), "getOSThreadId");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("rt", ".jfr");
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
        RecordedThread t = ev.getThread();
        check(t != null, "thread present");
        check(t.getJavaThreadId() > 0, "java thread id positive");
        check(t.getJavaName() != null, "java name");
        check(t.getId() > 0, "id positive");
        check(t.isVirtual() || !t.isVirtual(), "isVirtual callable");
    }

    // ---- deepening: explicit Thread field round-trip, platform vs virtual (JDK 21+) ----
    static Path file;

    static void recordFixture() throws Exception {
        file = Files.createTempFile("rtt", ".jfr");
        Thread vt = Thread.ofVirtual().name("rt-virtual").unstarted(() -> {});
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            vt.start();
            vt.join(3000);
            E onVirtual = new E(); onVirtual.thr = vt; onVirtual.commit();
            E onPlatform = new E(); onPlatform.thr = Thread.currentThread(); onPlatform.commit();
            r.stop();
            r.dump(file);
        }
    }

    static List<RecordedEvent> mine() throws Exception {
        List<RecordedEvent> out = new ArrayList<>();
        for (RecordedEvent e : RecordingFile.readAllEvents(file)) {
            if (e.getEventType().getName().equals("test.RecordedThread.E")) out.add(e);
        }
        return out;
    }

    static void testPlatformVsVirtual() throws Exception {
        recordFixture();
        try {
            List<RecordedEvent> evs = mine();
            check(evs.size() == 2, "two events recorded");
            RecordedThread platform = null, virtual = null;
            for (RecordedEvent e : evs) {
                RecordedThread rt = e.getThread("thr");
                if ("rt-virtual".equals(rt.getJavaName())) virtual = rt; else platform = rt;
            }
            // platform thread: full carrier semantics
            String creatorGroup = Thread.currentThread().getThreadGroup().getName();
            check(platform != null, "platform thread recorded");
            check(Thread.currentThread().getName().equals(platform.getJavaName()), "platform javaName == current thread");
            check(platform.getJavaThreadId() > 0, "platform javaThreadId > 0");
            check(platform.getId() == platform.getJavaThreadId(), "getId == getJavaThreadId for platform thread");
            check(platform.getOSThreadId() > 0, "platform osThreadId > 0");
            check(platform.getOSName() != null && platform.getOSName().equals(platform.getJavaName()), "platform osName == javaName");
            check(!platform.isVirtual(), "platform isVirtual false");
            check(platform.getThreadGroup() != null && creatorGroup.equals(platform.getThreadGroup().getName()),
                  "platform group == creating thread's group (was " + platform.getThreadGroup().getName() + ")");
            // virtual thread: recorded distinctly (JDK 21+ semantics)
            check(virtual != null, "virtual thread recorded");
            check(virtual.isVirtual(), "virtual isVirtual true");
            check("rt-virtual".equals(virtual.getJavaName()), "virtual javaName");
            check(virtual.getJavaThreadId() > 0, "virtual javaThreadId > 0");
            check(virtual.getOSName() == null, "virtual osName null");
            check(virtual.getOSThreadId() == -1, "virtual osThreadId -1");
            check(virtual.getThreadGroup() != null && "VirtualThreads".equals(virtual.getThreadGroup().getName()), "virtual group VirtualThreads");
            // implicit eventThread channel agrees for the platform commit
            RecordedEvent platformEv = null;
            for (RecordedEvent e : evs) {
                if (e.getThread("thr") == platform) platformEv = e;
            }
            check(platformEv.getThread().getJavaName().equals(platform.getJavaName()), "getThread() (implicit) == getThread(\"thr\")");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testPlatformVsVirtual();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

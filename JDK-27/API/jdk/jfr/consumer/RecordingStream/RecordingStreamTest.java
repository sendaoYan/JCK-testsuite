/*
 * @test
 * @summary jdk.jfr.consumer.RecordingStream conformance: white-box structure + black-box configure.
 * @run main RecordingStreamTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.*;

public class RecordingStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("test.RecordingStream.E")
    @Label("Recording Stream Event")
    static class E extends Event {
        @Label("id") long id;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordingStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "enable"), "enable");
        check(hasMethod(c, "disable"), "disable");
        check(hasMethod(c, "setSettings"), "setSettings");
        check(hasMethod(c, "setMaxAge"), "setMaxAge");
        check(hasMethod(c, "setMaxSize"), "setMaxSize");
        check(hasMethod(c, "setReuse"), "setReuse");
        check(hasMethod(c, "setOrdered"), "setOrdered");
        check(hasMethod(c, "onEvent"), "onEvent");
        check(hasMethod(c, "onFlush"), "onFlush");
        check(hasMethod(c, "onMetadata"), "onMetadata");
        check(hasMethod(c, "stop"), "stop");
        check(hasMethod(c, "dump"), "dump");
        check(hasMethod(c, "awaitTermination"), "awaitTermination");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        try (RecordingStream rs = new RecordingStream()) {
            check(rs != null, "construct");
            rs.enable("jdk.FileRead");
            rs.setReuse(true);
            rs.setOrdered(true);
            rs.onEvent(e -> {});
            rs.onFlush(() -> {});
            check(true, "configured");
        }
    }

    // ---- deepening: live streaming closed loop == committed events ----
    static void testLiveLoop() throws Exception {
        final int N = 3;
        try (RecordingStream rs = new RecordingStream()) {
            AtomicInteger n = new AtomicInteger();
            rs.enable(E.class).withoutStackTrace().withThreshold(Duration.ZERO);
            rs.onEvent("test.RecordingStream.E", e -> {
                if ("test.RecordingStream.E".equals(e.getEventType().getName())) n.incrementAndGet();
            });
            rs.startAsync();
            for (long i = 1; i <= N; i++) { E e = new E(); e.id = i; e.commit(); }
            long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
            while (n.get() < N && System.nanoTime() < deadline) Thread.sleep(20);
            check(n.get() == N, "onEvent received exactly " + N + " committed events (got " + n.get() + ")");
            check(rs.stop(), "stop() -> true");
            try { rs.stop(); check(false, "second stop should throw"); }
            catch (IllegalStateException expected) { check(true, "second stop() -> IllegalStateException"); }
        }
    }

    static void testDumpAndClose() throws Exception {
        final boolean[] closed = {false};
        Path d = Files.createTempFile("rsdump", ".jfr");
        try (RecordingStream rs = new RecordingStream()) {
            rs.onClose(() -> closed[0] = true);
            rs.setMaxSize(1_000_000);
            rs.startAsync();
            rs.dump(d);
            check(Files.size(d) > 0, "dump() writes a non-empty file");
        }
        check(closed[0], "onClose fired when stream closes");
        Files.deleteIfExists(d);
        // Configuration constructor
        try (RecordingStream rs = new RecordingStream(Configuration.getConfiguration("default"))) {
            check(rs != null, "RecordingStream(Configuration) construct");
        }
        // enable(Class) variant
        try (RecordingStream rs = new RecordingStream()) {
            EventSettings s = rs.enable(E.class);
            check(s != null, "enable(Class) returns EventSettings");
            EventSettings d2 = rs.disable(E.class);
            check(d2 != null, "disable(Class) returns EventSettings");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) {
            testLiveLoop();
            testDumpAndClose();
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

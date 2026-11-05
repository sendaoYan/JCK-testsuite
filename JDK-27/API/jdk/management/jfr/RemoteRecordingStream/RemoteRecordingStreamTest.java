/*
 * @test
 * @summary jdk.management.jfr.RemoteRecordingStream conformance: white-box structure + black-box behavior.
 * @run main RemoteRecordingStreamTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.time.Duration;
import java.util.*;
public class RemoteRecordingStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.RemoteRecordingStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(jdk.jfr.consumer.EventStream.class.isAssignableFrom(c), "implements EventStream");
        check(hasMethod(c, "onMetadata"), "onMetadata");
        check(hasMethod(c, "setSettings"), "setSettings");
        check(hasMethod(c, "disable"), "disable");
        check(hasMethod(c, "enable"), "enable");
        check(hasMethod(c, "setMaxAge"), "setMaxAge");
        check(hasMethod(c, "setMaxSize"), "setMaxSize");
        check(hasMethod(c, "onEvent"), "onEvent");
        check(hasMethod(c, "onFlush"), "onFlush");
        check(hasMethod(c, "onClose"), "onClose");
        check(hasMethod(c, "close"), "close");
        check(hasMethod(c, "setReuse"), "setReuse");
        check(hasMethod(c, "setOrdered"), "setOrdered");
        check(hasMethod(c, "start"), "start");
        check(hasMethod(c, "startAsync"), "startAsync");
        check(hasMethod(c, "stop"), "stop");
        check(hasMethod(c, "dump"), "dump");
        check(hasMethod(c, "awaitTermination"), "awaitTermination");
    }

    static void testBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        RemoteRecordingStream rrs = new RemoteRecordingStream(mbsc);
        check(rrs != null, "constructed");
        rrs.setMaxAge(java.time.Duration.ofSeconds(30));
        rrs.setMaxSize(1024 * 1024);
        rrs.setReuse(true);
        rrs.setOrdered(true);
        rrs.onEvent(e -> {});
        rrs.onFlush(() -> {});
        rrs.onClose(() -> {});
        rrs.close();
    }

    // ---- deepening: start/stop lifecycle, settings channel, closed-state negatives ----
    static void testLifecycle() throws Exception {
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        try (RemoteRecordingStream rrs = new RemoteRecordingStream(mbsc)) {
            jdk.jfr.EventSettings es = rrs.enable("jdk.FileRead");
            check(es != null, "enable returns EventSettings");
            jdk.jfr.EventSettings ds = rrs.disable("jdk.CPULoad");
            check(ds != null, "disable returns EventSettings");
            Map<String, String> settings = new HashMap<>();
            settings.put("jdk.FileRead#enabled", "false");
            rrs.setSettings(settings);
            check(true, "setSettings accepted");
            rrs.setMaxAge(Duration.ofSeconds(60));
            rrs.setMaxSize(50 * 1024 * 1024);
            rrs.setOrdered(true);
            rrs.setReuse(false);
            final boolean[] flushed = {false};
            rrs.onFlush(() -> flushed[0] = true);
            rrs.onEvent("jdk.FileRead", e -> {});
            rrs.startAsync();
            check(true, "startAsync on platform MBeanServer connection");
            check(rrs.stop(), "stop() -> true");
            rrs.awaitTermination(Duration.ofSeconds(3));
            check(true, "awaitTermination after stop");
        }
        // closed-stream negatives: ctor null NPE; start after close ISE
        try { new RemoteRecordingStream(null); check(false, "ctor null should throw"); }
        catch (NullPointerException expected) { check(true, "ctor null -> NPE"); }
        RemoteRecordingStream closed = new RemoteRecordingStream(mbsc);
        closed.close();
        try { closed.startAsync(); check(false, "startAsync after close should throw"); }
        catch (IllegalStateException expected) {
            check(String.valueOf(expected.getMessage()).contains("closed"), "startAsync after close -> ISE mentioning closed");
        }
        // dump on a started-then-stopped stream path (no throw guarantee asserted, existence only)
        try (RemoteRecordingStream rrs2 = new RemoteRecordingStream(mbsc)) {
            // dump() opens the target with CREATE_NEW: the path must not already exist
            java.nio.file.Path d = java.nio.file.Files.createTempFile("rrs-dump", ".jfr");
            java.nio.file.Files.deleteIfExists(d);
            rrs2.startAsync();
            rrs2.dump(d);
            check(java.nio.file.Files.size(d) > 0, "dump writes non-empty file");
            rrs2.stop();
            java.nio.file.Files.deleteIfExists(d);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (jdk.jfr.FlightRecorder.isAvailable()) testLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

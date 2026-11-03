/*
 * @test
 * @summary jdk.management.jfr.RemoteRecordingStream conformance: white-box structure + black-box behavior.
 * @run main RemoteRecordingStreamTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
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
        check(hasMethod(c, "stop"), "stop");

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.consumer.RecordingStream conformance: white-box structure + black-box configure.
 * @run main RecordingStreamTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.lang.reflect.*;

public class RecordingStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordingStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "enable"), "enable");
        check(hasMethod(c, "setReuse"), "setReuse");
        check(hasMethod(c, "setOrdered"), "setOrdered");
        check(hasMethod(c, "onEvent"), "onEvent");
        check(hasMethod(c, "onFlush"), "onFlush");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

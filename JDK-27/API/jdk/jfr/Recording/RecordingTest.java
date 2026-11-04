/*
 * @test
 * @summary jdk.jfr.Recording conformance: white-box structure + black-box create/start/stop/dump.
 * @run main RecordingTest
 */

import jdk.jfr.*;
import java.nio.file.*;
import java.lang.reflect.*;

public class RecordingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Recording");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "getState"), "getState");
        check(hasMethod(c, "start"), "start");
        check(hasMethod(c, "stop"), "stop");
        check(hasMethod(c, "dump"), "dump");
        check(hasMethod(c, "close"), "close");
        check(hasMethod(c, "enable"), "enable");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        Path p = Files.createTempFile("jfr-rec", ".jfr");
        try (Recording r = new Recording()) {
            check(r.getId() >= 0, "id non-negative");
            check(r.getState() == RecordingState.NEW, "initial state NEW");
            r.setName("conformance-test");
            check("conformance-test".equals(r.getName()), "name");
            r.enable("jdk.FileRead");
            check(r.getSettings() != null && !r.getSettings().isEmpty(), "settings populated");
            r.start();
            check(r.getState() == RecordingState.RUNNING, "state RUNNING");
            check(r.getStartTime() != null, "start time");
            check(r.stop(), "stop returns true");
            check(r.getState() == RecordingState.STOPPED, "state STOPPED");
            check(r.getStopTime() != null, "stop time");
            r.dump(p);
            check(Files.size(p) > 0, "dump non-empty");
            check(r.getSize() >= 0, "size non-negative");
        }
        Files.deleteIfExists(p);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

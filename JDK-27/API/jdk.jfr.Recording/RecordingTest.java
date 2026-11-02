/*
 * @test
 * @summary jdk.jfr.Recording conformance: create/start/stop/close/dump and settings.
 * @run main RecordingTest
 */

import jdk.jfr.*;
import java.nio.file.*;

public class RecordingTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        if (!FlightRecorder.isAvailable()) { System.out.println("PASS (0 assertions)"); return; }
        java.nio.file.Path p = Files.createTempFile("jfr-rec", ".jfr");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary jdk.jfr.Recording conformance: white-box structure + black-box create/start/stop/dump.
 * @run main RecordingTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.time.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

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
        testDeepDefaults();
        testDeepSetters();
        testDeepRecordSerializeReadBackLoop();
        testDeepStateMachine();
        testDeepScheduleStartAndDuration();
        testDeepDestination();
        testDeepSettingsChains();
        testDeepCtorsAndCopy();
        testDeepGetStream();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    interface IOAct { void run() throws IOException; }
    static void expectEx(String what, Class<? extends Throwable> type, String contains, Runnable r) {
        try { r.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }
    static void expectIO(String what, String contains, IOAct a) {
        try { a.run(); check(false, what + ": expected IOException"); }
        catch (IOException e) { check(String.valueOf(e.getMessage()).contains(contains), what + ": msg '" + e.getMessage() + "' lacks '" + contains + "'"); }
    }
    static void expectExIO(String what, Class<? extends Throwable> type, String contains, IOAct a) {
        try { a.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }
    static void awaitState(Recording r, RecordingState target) throws InterruptedException {
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (r.getState() != target && System.nanoTime() < deadline) Thread.sleep(10);
    }

    @Name("test.Recording.Loop") @Label("Loop") @Category({"cat"}) @Description("loop event")
    static class LoopEvent extends Event {
        @Label("id") long id;
        @Label("msg") String msg;
    }

    static void testDeepDefaults() throws Exception {
        try (Recording r = new Recording()) {
            check(r.getState() == RecordingState.NEW, "def state NEW");
            check(r.getSize() == 0L, "def size 0");
            check(r.getMaxAge() == null, "def maxAge null");
            check(r.getMaxSize() == 0L, "def maxSize 0");
            check(r.getDestination() == null, "def destination null");
            check(r.getDuration() == null, "def duration null");
            check(r.getDumpOnExit() == false, "def dumpOnExit false");
            check(r.isToDisk() == true, "def toDisk true");
            check(r.getSettings().isEmpty(), "def settings empty");
            check(r.getStartTime() == null, "def startTime null");
            check(r.getStopTime() == null, "def stopTime null");
            check(r.getName() != null, "def name non-null");
            check(r.getId() > 0, "id > 0");
        }
    }

    static void testDeepSetters() throws Exception {
        try (Recording r = new Recording()) {
            r.setName("deep");
            check("deep".equals(r.getName()), "setName roundtrip");
            expectEx("setName(null)", NullPointerException.class, null, () -> r.setName(null));
            r.setToDisk(false); check(r.isToDisk() == false, "setToDisk roundtrip");
            r.setToDisk(true); check(r.isToDisk() == true, "setToDisk roundtrip true");
            r.setDumpOnExit(true); check(r.getDumpOnExit() == true, "setDumpOnExit roundtrip");
            r.setDuration(Duration.ofSeconds(9));
            check(Duration.ofSeconds(9).equals(r.getDuration()), "setDuration roundtrip");
            r.setMaxSize(1000L); check(r.getMaxSize() == 1000L, "setMaxSize roundtrip");
            r.setMaxAge(Duration.ofSeconds(8));
            check(Duration.ofSeconds(8).equals(r.getMaxAge()), "setMaxAge roundtrip");
            expectEx("setMaxSize(-1)", IllegalArgumentException.class, "Max size of recording can't be negative", () -> r.setMaxSize(-1L));
            check(r.getMaxSize() == 1000L, "maxSize unchanged after negative set");
            expectEx("setMaxAge(neg)", IllegalArgumentException.class, "Max age of recording can't be negative", () -> r.setMaxAge(Duration.ofSeconds(-1)));
            check(r.getState() == RecordingState.NEW, "state NEW after bad sets");
        }
    }

    static void testDeepRecordSerializeReadBackLoop() throws Exception {
        Path tmp = Files.createTempFile("rec-loop", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable("test.Recording.Loop").withThreshold(Duration.ZERO);
                r.start();
                LoopEvent e1 = new LoopEvent(); e1.id = 1; e1.msg = "m1"; e1.commit();
                LoopEvent e2 = new LoopEvent(); e2.id = 2; e2.msg = "m2"; e2.commit();
                check(r.stop(), "loop stop returns true");
                r.dump(tmp);
                check(Files.size(tmp) > 0, "loop dump non-empty");
            }
            List<RecordedEvent> all = RecordingFile.readAllEvents(tmp);
            List<RecordedEvent> mine = new ArrayList<RecordedEvent>();
            for (RecordedEvent re : all) if (re.getEventType().getName().equals("test.Recording.Loop")) mine.add(re);
            check(mine.size() == 2, "loop: exactly 2 custom events, got " + mine.size());
            long id1 = mine.get(0).getLong("id"), id2 = mine.get(1).getLong("id");
            check((id1 == 1 && id2 == 2) || (id1 == 2 && id2 == 1), "loop ids are {1,2}");
            String m1 = mine.get(0).getString("msg"), m2 = mine.get(1).getString("msg");
            check((("m1".equals(m1)) && id1 == 1) || (("m2".equals(m1)) && id1 == 2), "loop msg/id pairing 1");
            check((("m1".equals(m2)) && id2 == 1) || (("m2".equals(m2)) && id2 == 2), "loop msg/id pairing 2");
            for (RecordedEvent re : mine) {
                check(re.getStartTime().compareTo(re.getEndTime()) <= 0, "loop start<=end");
                check(!re.getDuration().isNegative(), "loop duration>=0");
                check(re.getThread() != null, "loop eventThread non-null");
                check(re.hasField("id") && re.hasField("msg"), "loop hasField id/msg");
                check(!re.hasField("nope"), "loop hasField(nope) false");
                check("test.Recording.Loop".equals(re.getEventType().getName()), "loop eventType name");
            }
            // streaming equivalence: RecordingFile stream count == readAllEvents count
            int streamed = 0;
            try (RecordingFile rf = new RecordingFile(tmp)) {
                while (rf.hasMoreEvents()) {
                    if (rf.readEvent().getEventType().getName().equals("test.Recording.Loop")) streamed++;
                }
            }
            check(streamed == 2, "loop streaming count == 2");
            // file re-readable: second full read sees identical total
            check(RecordingFile.readAllEvents(tmp).size() == all.size(), "loop file re-readable");
        } finally { Files.deleteIfExists(tmp); }
    }

    static void testDeepStateMachine() throws Exception {
        Recording r = new Recording();
        try {
            expectEx("stop NEW", IllegalStateException.class, "Recording must be started before it can be stopped.", r::stop);
            r.start();
            check(r.getState() == RecordingState.RUNNING, "RUNNING while running");
            expectEx("start RUNNING", IllegalStateException.class, "Recording can only be started once.", r::start);
            check(r.stop(), "stop returns true");
            check(r.getState() == RecordingState.STOPPED, "STOPPED after stop");
            expectEx("stop STOPPED", IllegalStateException.class, "Can't stop an already stopped recording.", r::stop);
            expectEx("start STOPPED", IllegalStateException.class, "Recording can only be started once.", r::start);
            r.close();
            check(r.getState() == RecordingState.CLOSED, "CLOSED after close");
            r.close();
            check(true, "close idempotent");
            expectEx("start CLOSED", IllegalStateException.class, null, r::start);
            expectEx("stop CLOSED", IllegalStateException.class, null, r::stop);
        } finally { r.close(); }
        Recording r2 = new Recording();
        Path p = Files.createTempFile("rec-nostart", ".jfr");
        try {
            expectIO("dump NEW", "has not started", () -> r2.dump(p));
        } finally { r2.close(); Files.deleteIfExists(p); }
        Recording r3 = new Recording();
        Path p3 = Files.createTempFile("rec-closed", ".jfr");
        try {
            r3.start(); r3.stop(); r3.close();
            expectIO("dump CLOSED", "has been closed", () -> r3.dump(p3));
            expectIO("getStream CLOSED", "Recording must be stopped before it can be read.", () -> r3.getStream(Instant.EPOCH, Instant.now()));
        } finally { Files.deleteIfExists(p3); }
    }

    static void testDeepScheduleStartAndDuration() throws Exception {
        try (Recording r = new Recording()) {
            r.scheduleStart(Duration.ofMillis(150));
            check(r.getState() == RecordingState.DELAYED, "DELAYED right after scheduleStart");
            awaitState(r, RecordingState.RUNNING);
            check(r.getState() == RecordingState.RUNNING, "RUNNING after bounded wait");
            check(r.getStartTime() != null, "startTime set when running");
        }
        try (Recording r = new Recording()) {
            r.setDuration(Duration.ofMillis(150));
            r.start();
            awaitState(r, RecordingState.STOPPED);
            check(r.getState() == RecordingState.STOPPED, "STOPPED after duration elapsed");
            check(r.getStopTime() != null, "stopTime set after duration");
        }
    }

    static void testDeepDestination() throws Exception {
        Path dir = Files.createTempDirectory("rec-dest");
        try {
            Path dest = dir.resolve("auto.jfr");
            try (Recording r = new Recording()) {
                r.setDestination(dest);
                check(dest.equals(r.getDestination()), "destination roundtrip");
                r.setDestination(null);
                check(r.getDestination() == null, "destination cleared with null");
                r.setDestination(dest);
                r.enable("test.Recording.Loop").withThreshold(Duration.ZERO);
                r.start();
                LoopEvent e = new LoopEvent(); e.id = 7; e.msg = "d"; e.commit();
                r.stop();
                check(Files.exists(dest), "destination auto-dumped on stop");
                check(Files.size(dest) > 0, "destination auto-dump non-empty");
                check(r.getState() == RecordingState.CLOSED, "recording auto-closed after destination write");
                Path extra = dir.resolve("extra.jfr");
                expectIO("dump after destination auto-close", "has been closed", () -> r.dump(extra));
            }
        } finally {
            Files.list(dir).forEach(f -> { try { Files.deleteIfExists(f); } catch (IOException ignore) {} });
            Files.deleteIfExists(dir);
        }
    }

    static void testDeepSettingsChains() throws Exception {
        try (Recording r = new Recording()) {
            EventSettings es = r.enable("jdk.CPULoad").with("period", "10 ms");
            check(es != null, "enable().with() returns settings");
            check("10 ms".equals(r.getSettings().get("jdk.CPULoad#period")), "period key value '10 ms'");
            r.enable("test.Recording.Loop").withThreshold(Duration.ofMillis(3));
            check("3000000 ns".equals(r.getSettings().get("test.Recording.Loop#threshold")), "withThreshold(3ms) -> 3000000 ns");
            r.enable("test.Recording.Loop").withoutThreshold();
            check("0 s".equals(r.getSettings().get("test.Recording.Loop#threshold")), "withoutThreshold -> 0 s");
            r.enable("test.Recording.Loop").withPeriod(Duration.ofSeconds(5));
            check("5000000000 ns".equals(r.getSettings().get("test.Recording.Loop#period")), "withPeriod(5s) -> 5000000000 ns");
            r.enable("test.Recording.Loop").withoutStackTrace();
            check("false".equals(r.getSettings().get("test.Recording.Loop#stackTrace")), "withoutStackTrace -> false");
            r.enable("test.Recording.Loop").withStackTrace();
            check("true".equals(r.getSettings().get("test.Recording.Loop#stackTrace")), "withStackTrace -> true");
            r.disable("jdk.CPULoad");
            check("false".equals(r.getSettings().get("jdk.CPULoad#enabled")), "disable -> enabled false");
            check("10 ms".equals(r.getSettings().get("jdk.CPULoad#period")), "period retained after disable");
            r.enable("no.such.Event");
            check("true".equals(r.getSettings().get("no.such.Event#enabled")), "enable(unknown name) tolerated");
            r.enable(LoopEvent.class);
            boolean numericKey = false;
            for (String k : r.getSettings().keySet()) if (k.matches("\\d+#enabled") && "true".equals(r.getSettings().get(k))) numericKey = true;
            check(numericKey, "enable(Class) writes <typeId>#enabled=true");
            Map<String, String> snap = r.getSettings();
            int before = snap.size();
            snap.put("hacked", "x");
            check(!r.getSettings().containsKey("hacked") && r.getSettings().size() == before, "getSettings returns a copy");
        }
    }

    static void testDeepCtorsAndCopy() throws Exception {
        Map<String, String> m = new HashMap<String, String>();
        m.put("test.Recording.Loop#enabled", "true");
        try (Recording r = new Recording(m)) {
            check("true".equals(r.getSettings().get("test.Recording.Loop#enabled")), "Map ctor settings visible");
            check(r.getState() == RecordingState.NEW, "Map ctor state NEW");
            Map<String, String> m2 = new HashMap<String, String>();
            m2.put("test.Recording.Loop#enabled", "false");
            r.setSettings(m2);
            check("false".equals(r.getSettings().get("test.Recording.Loop#enabled")), "setSettings replaces");
        }
        try (Recording r = new Recording(Configuration.getConfiguration("default"))) {
            check(!r.getSettings().isEmpty(), "Configuration ctor settings non-empty");
            check(r.getState() == RecordingState.NEW, "Configuration ctor state NEW");
        }
        try (Recording src = new Recording()) {
            src.enable("test.Recording.Loop").withThreshold(Duration.ofMillis(3));
            Recording c = src.copy(false);
            try {
                check(c.getState() == RecordingState.NEW, "copy(false) from NEW is NEW");
                check("3000000 ns".equals(c.getSettings().get("test.Recording.Loop#threshold")), "copy keeps settings");
                check(c.getId() != src.getId(), "copy id differs from source");
            } finally { c.close(); }
            src.start();
            Recording c2 = src.copy(true);
            try { check(c2.getState() == RecordingState.STOPPED, "copy(true) from RUNNING is STOPPED"); } finally { c2.close(); }
            Recording c3 = src.copy(false);
            try { check(c3.getState() == RecordingState.RUNNING, "copy(false) from RUNNING is RUNNING"); } finally { c3.close(); }
            src.stop();
        }
    }

    static void testDeepGetStream() throws Exception {
        try (Recording r = new Recording()) {
            r.start();
            expectIO("getStream while RUNNING", "Recording must be stopped before it can be read.", () -> r.getStream(Instant.EPOCH, Instant.now()));
            r.stop();
            try (InputStream is = r.getStream(Instant.EPOCH, Instant.now())) {
                check(is != null, "getStream post-stop non-null");
                check(is.read() != -1, "stream readable first byte");
                check(is.read(new byte[64]) > 0, "stream has body bytes");
            }
            expectExIO("getStream bad range", IllegalArgumentException.class, "End time of requested stream must not be before start time", () -> r.getStream(Instant.now(), Instant.EPOCH));
        }
    }
}

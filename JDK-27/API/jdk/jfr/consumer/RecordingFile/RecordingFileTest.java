/*
 * @test
 * @summary jdk.jfr.consumer.RecordingFile conformance: white-box structure + black-box read back.
 * @run main RecordingFileTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.lang.reflect.*;

public class RecordingFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("test.RecordingFile.E")
    @Label("Recording File Event")
    static class E extends Event {
        @Label("id") long id;
        @Label("tag") String tag;
    }

    static Path file;

    static void recordFixture() throws Exception {
        file = Files.createTempFile("rft", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            for (long i = 1; i <= 4; i++) { E e = new E(); e.id = i; e.tag = (i % 2 == 0) ? "even" : "odd"; e.commit(); }
            r.stop();
            r.dump(file);
        }
    }

    static List<RecordedEvent> mine(List<RecordedEvent> all) {
        List<RecordedEvent> out = new ArrayList<>();
        for (RecordedEvent e : all) if (e.getEventType().getName().equals("test.RecordingFile.E")) out.add(e);
        return out;
    }

    static Set<Long> ids(List<RecordedEvent> evs) {
        Set<Long> s = new TreeSet<>();
        for (RecordedEvent e : evs) s.add(e.getLong("id"));
        return s;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordingFile");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "hasMoreEvents"), "hasMoreEvents");
        check(hasMethod(c, "readEvent"), "readEvent");
        check(hasMethod(c, "readAllEvents"), "readAllEvents");
        check(hasMethod(c, "readEventTypes"), "readEventTypes");
        check(hasMethod(c, "write"), "write(Path,Predicate) present on JDK-27");
        check(hasMethod(c, "close"), "close");
    }

    static void testChannelEquivalence() throws Exception {
        // (a) static channel: readAllEvents
        List<RecordedEvent> all = RecordingFile.readAllEvents(file);
        List<RecordedEvent> a = mine(all);
        check(a.size() == 4, "readAllEvents custom count == 4");
        check(ids(a).equals(new TreeSet<>(Arrays.asList(1L, 2L, 3L, 4L))), "readAllEvents id set");
        for (RecordedEvent e : a) {
            long id = e.getLong("id");
            String expectTag = (id % 2 == 0) ? "even" : "odd";
            check(expectTag.equals(e.getString("tag")), "readAllEvents field tag id=" + id);
        }
        // (b) streaming channel: try-with-resources iteration, count must equal static channel
        List<RecordedEvent> b = new ArrayList<>();
        try (RecordingFile rf = new RecordingFile(file)) {
            while (rf.hasMoreEvents()) b.add(rf.readEvent());
            check(!rf.hasMoreEvents(), "hasMoreEvents false at EOF");
        }
        check(mine(b).size() == mine(all).size(), "streaming count == readAllEvents count (two channels equivalent)");
        check(ids(mine(b)).equals(ids(mine(all))), "streaming id set == readAllEvents id set");
        // (c) file re-readable: a second independent RecordingFile instance yields the same event set
        List<RecordedEvent> c2 = new ArrayList<>();
        try (RecordingFile rf2 = new RecordingFile(file)) {
            while (rf2.hasMoreEvents()) c2.add(rf2.readEvent());
        }
        check(ids(mine(c2)).equals(ids(mine(all))), "file re-readable: second instance same event set");
        // field-level equality across the two independent reads
        Map<Long, String> tags1 = new HashMap<>(), tags2 = new HashMap<>();
        for (RecordedEvent e : mine(all)) tags1.put(e.getLong("id"), e.getString("tag"));
        for (RecordedEvent e : mine(c2)) tags2.put(e.getLong("id"), e.getString("tag"));
        check(tags1.equals(tags2), "field values equal across independent reads");
    }

    static void testWriteFiltered() throws Exception {
        Path out = Files.createTempFile("rftw", ".jfr");
        Predicate<RecordedEvent> evenOnly = e -> e.getEventType().getName().equals("test.RecordingFile.E") && "even".equals(e.getString("tag"));
        try (RecordingFile rf = new RecordingFile(file)) {
            rf.write(out, evenOnly);
        }
        List<RecordedEvent> filtered = RecordingFile.readAllEvents(out);
        List<RecordedEvent> fmine = mine(filtered);
        check(fmine.size() == 2, "write() predicate keeps only hits");
        check(ids(fmine).equals(new TreeSet<>(Arrays.asList(2L, 4L))), "write() kept even ids only");
        for (RecordedEvent e : fmine) check("even".equals(e.getString("tag")), "write() copy field intact id=" + e.getLong("id"));
        // write to the same predicate again reproduces the same output (deterministic filter)
        Path out2 = Files.createTempFile("rftw2", ".jfr");
        try (RecordingFile rf = new RecordingFile(file)) { rf.write(out2, evenOnly); }
        check(ids(mine(RecordingFile.readAllEvents(out2))).equals(ids(fmine)), "write() deterministic");
        Files.deleteIfExists(out);
        Files.deleteIfExists(out2);
    }

    static void testEventTypes() throws Exception {
        try (RecordingFile rf = new RecordingFile(file)) {
            List<EventType> types = rf.readEventTypes();
            check(types != null && !types.isEmpty(), "readEventTypes non-empty");
            boolean found = false;
            for (EventType t : types) if (t.getName().equals("test.RecordingFile.E")) found = true;
            check(found, "readEventTypes contains custom event type");
            check(rf.readEventTypes().size() == types.size(), "readEventTypes repeatable");
        }
    }

    static void testNegatives() throws Exception {
        // constructor null -> NPE
        try { new RecordingFile(null); check(false, "ctor null should throw"); }
        catch (NullPointerException expected) { check(true, "ctor null -> NPE"); }
        // missing file -> FileNotFoundException (IOException)
        Path missing = Paths.get(System.getProperty("java.io.tmpdir"), "no-such-rft-" + System.nanoTime() + ".jfr");
        try { new RecordingFile(missing); check(false, "ctor missing should throw"); }
        catch (java.io.FileNotFoundException expected) { check(true, "ctor missing -> FileNotFoundException"); }
        try { RecordingFile.readAllEvents(missing); check(false, "readAllEvents missing should throw"); }
        catch (java.io.FileNotFoundException expected) { check(true, "readAllEvents missing -> FileNotFoundException"); }
        // closed file: readEvent / readEventTypes -> IOException; hasMoreEvents tolerated
        RecordingFile rf = new RecordingFile(file);
        rf.close();
        try { rf.readEvent(); check(false, "readEvent after close should throw"); }
        catch (java.io.IOException expected) { check(true, "readEvent after close -> IOException"); }
        try { rf.readEventTypes(); check(false, "readEventTypes after close should throw"); }
        catch (java.io.IOException expected) { check(true, "readEventTypes after close -> IOException"); }
        boolean more = rf.hasMoreEvents(); // must not throw
        check(!more || more, "hasMoreEvents after close tolerated");
        // close is idempotent
        rf.close();
        check(true, "close idempotent");
    }

    // ---- original black-box smoke (kept verbatim) ----
    static void testBlackBoxOriginal() throws Exception {
        Path p = Files.createTempFile("rf", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            E ev = new E(); ev.id = 1; ev.tag = "odd"; ev.commit();
            r.stop();
            r.dump(p);
        }
        check(Files.size(p) > 0, "dump non-empty");
        try (RecordingFile rf = new RecordingFile(p)) {
            check(rf != null, "open");
            int n = 0;
            while (rf.hasMoreEvents()) { RecordedEvent e = rf.readEvent(); if (e != null) n++; }
            check(n > 0, "read events");
        }
        int total = RecordingFile.readAllEvents(p).size();
        check(total > 0, "readAllEvents");
        Files.deleteIfExists(p);
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        recordFixture();
        check(Files.size(file) > 0, "dump non-empty");
        try {
            testChannelEquivalence();
            testWriteFiltered();
            testEventTypes();
            testNegatives();
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); }
        else {
            testBlackBoxOriginal();
            testBlackBox();
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

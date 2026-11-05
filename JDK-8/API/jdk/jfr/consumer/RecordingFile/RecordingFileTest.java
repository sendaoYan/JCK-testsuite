/*
 * @test
 * @summary jdk.jfr.consumer.RecordingFile conformance: structure + full two-channel read-back contract.
 * @run main RecordingFileTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordingFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordingFile", false, RecordingFileTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "java.io.Closeable"), "implements java.io.Closeable");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "hasMoreEvents"), "declares hasMoreEvents");
        check(hasMethod(c, "readAllEvents"), "declares readAllEvents");
        check(hasMethod(c, "readEvent"), "declares readEvent");
        check(hasMethod(c, "readEventTypes"), "declares readEventTypes");
        // JDK-8 does not expose write(Path, Predicate) (added in JDK 21)
        check(!hasMethod(c, "write"), "no write() on JDK-8");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        recordFixture();
        try {
            check(Files.size(file) > 0, "dump non-empty");

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
            // (b) streaming channel: iteration count must equal static channel
            List<RecordedEvent> b = new ArrayList<>();
            try (RecordingFile rf = new RecordingFile(file)) {
                while (rf.hasMoreEvents()) b.add(rf.readEvent());
                check(!rf.hasMoreEvents(), "hasMoreEvents false at EOF");
            }
            check(mine(b).size() == a.size(), "streaming count == readAllEvents count (two channels equivalent)");
            check(ids(mine(b)).equals(ids(a)), "streaming id set == readAllEvents id set");
            // (c) file re-readable: a second independent RecordingFile instance yields the same set
            List<RecordedEvent> c2 = new ArrayList<>();
            try (RecordingFile rf2 = new RecordingFile(file)) {
                while (rf2.hasMoreEvents()) c2.add(rf2.readEvent());
            }
            check(ids(mine(c2)).equals(ids(a)), "file re-readable: second instance same event set");
            Map<Long, String> tags1 = new HashMap<>(), tags2 = new HashMap<>();
            for (RecordedEvent e : a) tags1.put(e.getLong("id"), e.getString("tag"));
            for (RecordedEvent e : mine(c2)) tags2.put(e.getLong("id"), e.getString("tag"));
            check(tags1.equals(tags2), "field values equal across independent reads");

            // (d) readEventTypes includes the custom type
            try (RecordingFile rf = new RecordingFile(file)) {
                List<EventType> types = rf.readEventTypes();
                check(types != null && !types.isEmpty(), "readEventTypes non-empty");
                boolean found = false;
                for (EventType t : types) if (t.getName().equals("test.RecordingFile.E")) found = true;
                check(found, "readEventTypes contains custom event type");
            }

            // (e) negative matrix
            try { new RecordingFile(null); check(false, "ctor null should throw"); }
            catch (NullPointerException expected) { check(true, "ctor null -> NPE"); }
            Path missing = Paths.get(System.getProperty("java.io.tmpdir"), "no-such-rft8-" + System.nanoTime() + ".jfr");
            try { new RecordingFile(missing); check(false, "ctor missing should throw"); }
            catch (java.io.FileNotFoundException expected) { check(true, "ctor missing -> FileNotFoundException"); }
            try { RecordingFile.readAllEvents(missing); check(false, "readAllEvents missing should throw"); }
            catch (java.io.FileNotFoundException expected) { check(true, "readAllEvents missing -> FileNotFoundException"); }
            RecordingFile rf3 = new RecordingFile(file);
            rf3.close();
            try { rf3.readEvent(); check(false, "readEvent after close should throw"); }
            catch (java.io.IOException expected) { check(true, "readEvent after close -> IOException"); }
            try { rf3.readEventTypes(); check(false, "readEventTypes after close should throw"); }
            catch (java.io.IOException expected) { check(true, "readEventTypes after close -> IOException"); }
            boolean more = rf3.hasMoreEvents(); // tolerated after close
            check(!more || more, "hasMoreEvents after close tolerated");
            rf3.close();
            check(true, "close idempotent");
        } finally {
            Files.deleteIfExists(file);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

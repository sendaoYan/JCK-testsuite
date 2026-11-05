/*
 * @test
 * @summary jdk.jfr.consumer.EventStream conformance: white-box structure + black-box openRepository.
 * @run main EventStreamTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.lang.reflect.*;

public class EventStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("test.EventStream.E")
    @Label("Event Stream Event")
    static class E extends Event {
        @Label("id") long id;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.EventStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "openRepository"), "openRepository");
        check(hasMethod(c, "openFile"), "openFile");
        check(hasMethod(c, "onEvent"), "onEvent");
        check(hasMethod(c, "onMetadata"), "onMetadata");
        check(hasMethod(c, "onClose"), "onClose");
        check(hasMethod(c, "setReuse"), "setReuse");
        check(hasMethod(c, "setOrdered"), "setOrdered");
        check(hasMethod(c, "setStartTime"), "setStartTime");
        check(hasMethod(c, "setEndTime"), "setEndTime");
        check(hasMethod(c, "remove"), "remove");
        check(hasMethod(c, "awaitTermination"), "awaitTermination");
        check(c != null && EventStream.class == c, "interface identity");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        try (EventStream es = EventStream.openRepository()) {
            check(es != null, "openRepository");
            es.onEvent(e -> {});
            es.onClose(() -> {});
            es.setReuse(true);
            es.setOrdered(true);
            check(true, "configured");
        }
    }

    // ---- deepening: file streaming channel == readAllEvents channel ----
    static Path dump(int n) throws Exception {
        Path f = Files.createTempFile("est", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            for (long i = 1; i <= n; i++) { E e = new E(); e.id = i; e.commit(); }
            r.stop();
            r.dump(f);
        }
        return f;
    }

    static int countMine(Path f, boolean blockingStart) throws Exception {
        AtomicInteger cnt = new AtomicInteger();
        try (EventStream es = EventStream.openFile(f)) {
            es.onEvent(e -> { if (e.getEventType().getName().equals("test.EventStream.E")) cnt.incrementAndGet(); });
            if (blockingStart) es.start();
            else { es.startAsync(); es.awaitTermination(java.time.Duration.ofSeconds(3)); }
        }
        return cnt.get();
    }

    static void testFileChannelEquivalence() throws Exception {
        Path f = dump(5);
        try {
            int staticCount = 0;
            for (RecordedEvent e : RecordingFile.readAllEvents(f)) {
                if (e.getEventType().getName().equals("test.EventStream.E")) staticCount++;
            }
            check(staticCount == 5, "readAllEvents baseline == 5");
            check(countMine(f, true) == staticCount, "EventStream.openFile + start() count == readAllEvents count");
            check(countMine(f, false) == staticCount, "EventStream.openFile + startAsync/awaitTermination count == readAllEvents count");
        } finally {
            Files.deleteIfExists(f);
        }
    }

    static void testRemoveSemantics() throws Exception {
        Path f = dump(5);
        try {
            AtomicInteger kept = new AtomicInteger(), removed = new AtomicInteger();
            try (EventStream es = EventStream.openFile(f)) {
                Consumer<RecordedEvent> keep = e -> { if (e.getEventType().getName().equals("test.EventStream.E")) kept.incrementAndGet(); };
                Consumer<RecordedEvent> drop = e -> removed.incrementAndGet();
                es.onEvent("test.EventStream.E", keep);
                es.onEvent("test.EventStream.E", drop);
                check(es.remove(drop), "remove(registered handler) -> true");
                check(!es.remove(new Object()), "remove(unknown object) -> false");
                es.start();
            }
            check(kept.get() == 5, "kept handler received all 5 events");
            check(removed.get() == 0, "removed handler received none");
        } finally {
            Files.deleteIfExists(f);
        }
    }

    static void testOrderingReuseAndClose() throws Exception {
        Path f = dump(3);
        try {
            AtomicInteger cnt = new AtomicInteger();
            final boolean[] closed = {false};
            try (EventStream es = EventStream.openFile(f)) {
                es.setOrdered(true);
                es.setReuse(false);
                es.onClose(() -> closed[0] = true);
                es.onEvent("test.EventStream.E", e -> cnt.incrementAndGet());
                es.start();
            }
            check(cnt.get() == 3, "setOrdered/setReuse path delivers all events");
            check(closed[0], "onClose callback fired on close");
            // metadata delivery via file stream
            final List<MetadataEvent> metas = new ArrayList<>();
            try (EventStream es = EventStream.openFile(f)) {
                es.onMetadata(metas::add);
                es.onEvent(e -> {});
                es.start();
            }
            check(metas.size() >= 1, "onMetadata fired for file stream");
            if (!metas.isEmpty()) {
                boolean hasCustom = false;
                for (EventType t : metas.get(0).getEventTypes()) {
                    if ("test.EventStream.E".equals(t.getName())) hasCustom = true;
                }
                check(hasCustom, "metadata event types include custom event");
            }
        } finally {
            Files.deleteIfExists(f);
        }
    }

    static void testNegatives() throws Exception {
        try { EventStream.openRepository(null); check(false, "openRepository(null) should throw"); }
        catch (NullPointerException expected) { check(true, "openRepository(null) -> NPE"); }
        Path missing = Paths.get(System.getProperty("java.io.tmpdir"), "no-such-est-" + System.nanoTime() + ".jfr");
        try { EventStream.openFile(missing); check(false, "openFile(missing) should throw"); }
        catch (java.io.FileNotFoundException expected) { check(true, "openFile(missing) -> FileNotFoundException"); }
        // close() on a never-started stream is tolerated (try-with-resources path)
        try (EventStream es = EventStream.openRepository()) {
            es.close(); // explicit early close inside try-with-resources; second close is a no-op
            check(true, "close before start tolerated");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) {
            testFileChannelEquivalence();
            testRemoveSemantics();
            testOrderingReuseAndClose();
        }
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

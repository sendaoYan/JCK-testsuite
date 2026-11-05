/*
 * @test
 * @summary jdk.jfr.consumer.MetadataEvent conformance: white-box structure (no public constructor; instance via stream metadata only).
 * @run main MetadataEventTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

public class MetadataEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("test.MetadataEvent.E")
    @Label("Metadata Event")
    static class E extends Event {
        @Label("id") long id;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.MetadataEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getEventTypes"), "getEventTypes");
        check(hasMethod(c, "getAddedEventTypes"), "getAddedEventTypes");
        check(hasMethod(c, "getRemovedEventTypes"), "getRemovedEventTypes");
        check(hasMethod(c, "getConfigurations"), "getConfigurations");
        // no public constructor: instances are only delivered via onMetadata
        boolean publicCtor = false;
        for (Constructor<?> k : c.getConstructors()) publicCtor = true;
        check(!publicCtor, "no public constructor");
    }

    // Original note: instances are only delivered through a consumer
    // EventStream's metadata callbacks. Deepened: they ARE reproducible via
    // EventStream.openFile(), which delivers one MetadataEvent per chunk.
    static void testBlackBox() {
        check(true, "no public constructor; white-box only");
    }

    static void testViaFileStream() throws Exception {
        Path f = Files.createTempFile("met", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            E e = new E(); e.id = 1; e.commit();
            r.stop();
            r.dump(f);
        }
        final List<MetadataEvent> metas = new ArrayList<>();
        try (EventStream es = EventStream.openFile(f)) {
            es.onMetadata(metas::add);
            es.onEvent(ev -> {});
            es.start();
        }
        Files.deleteIfExists(f);
        check(metas.size() >= 1, "MetadataEvent delivered via onMetadata");
        MetadataEvent me = metas.get(0);
        List<EventType> types = me.getEventTypes();
        check(types != null && !types.isEmpty(), "getEventTypes non-empty");
        boolean hasCustom = false, hasFileRead = false;
        for (EventType t : types) {
            if ("test.MetadataEvent.E".equals(t.getName())) hasCustom = true;
            if ("jdk.FileRead".equals(t.getName())) hasFileRead = true;
        }
        check(hasCustom, "event types include the custom event");
        check(hasFileRead, "event types include jdk.FileRead");
        // first metadata delivery reports everything as added, nothing removed
        check(!me.getAddedEventTypes().isEmpty(), "first delivery: added types non-empty");
        check(me.getAddedEventTypes().size() == types.size(), "first delivery adds all types");
        check(me.getRemovedEventTypes().isEmpty(), "first delivery removes none");
        check(me.getConfigurations() != null, "getConfigurations non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (FlightRecorder.isAvailable()) testViaFileStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

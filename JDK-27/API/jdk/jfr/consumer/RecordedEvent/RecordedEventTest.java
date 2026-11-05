/*
 * @test
 * @summary jdk.jfr.consumer.RecordedEvent conformance: white-box structure + black-box field read.
 * @run main RecordedEventTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

public class RecordedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("test.RecordedEvent.E")
    @Label("Recorded Event")
    @Category({"Test Cat"})
    static class E extends Event {
        @Label("message") String message;
    }

    @Name("test.RecordedEvent.NoStack")
    @Label("No Stack")
    static class NoStack extends Event {
        @Label("v") long v;
    }

    // ---- original black-box smoke (kept verbatim) ----
    static void testBlackBoxOriginal() throws Exception {
        Path p = Files.createTempFile("re", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class);
            r.start();
            E ev = new E(); ev.message = "hello"; ev.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if ("test.RecordedEvent.E".equals(e.getEventType().getName())) { ev = e; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "found custom event");
        check(ev.getEventType() != null, "event type");
        check(ev.getStartTime() != null, "start time");
        check(ev.getDuration() != null, "duration");
        check(ev.hasField("message"), "has message field");
        check("hello".equals(ev.getString("message")), "message value");
        check(ev.getFields() != null, "fields");
    }

    static Path file;

    static void recordFixture() throws Exception {
        file = Files.createTempFile("ret", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.enable(NoStack.class).withoutStackTrace();
            r.start();
            E ev = new E(); ev.message = "hello"; ev.commit();
            NoStack ns = new NoStack(); ns.v = 7; ns.commit();
            r.stop();
            r.dump(file);
        }
    }

    static RecordedEvent find(String name) throws Exception {
        for (RecordedEvent e : RecordingFile.readAllEvents(file)) {
            if (e.getEventType().getName().equals(name)) return e;
        }
        return null;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getEventType"), "getEventType");
        check(hasMethod(c, "getStartTime"), "getStartTime");
        check(hasMethod(c, "getEndTime"), "getEndTime");
        check(hasMethod(c, "getDuration"), "getDuration");
        check(hasMethod(c, "getString"), "getString");
        check(hasMethod(c, "hasField"), "hasField");
        check(hasMethod(c, "getStackTrace"), "getStackTrace");
        check(hasMethod(c, "getThread"), "getThread");
        check(hasMethod(c, "getFields"), "getFields");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        recordFixture();
        try {
            RecordedEvent ev = find("test.RecordedEvent.E");
            check(ev != null, "found custom event");
            // metadata channel: getEventType() must reflect the annotation-derived metadata
            EventType t = ev.getEventType();
            check(t != null, "event type");
            check("test.RecordedEvent.E".equals(t.getName()), "event type name from @Name");
            check("Recorded Event".equals(t.getLabel()), "event type label from @Label");
            check(t.getCategoryNames().contains("Test Cat"), "event type category from @Category");
            check(t.getId() > 0, "event type id > 0");
            // time semantics
            check(ev.getStartTime() != null, "start time");
            check(ev.getEndTime() != null, "end time");
            check(!ev.getStartTime().isAfter(ev.getEndTime()), "start <= end");
            check(ev.getDuration() != null && !ev.getDuration().isNegative(), "duration >= 0");
            check(ev.getEndTime().equals(ev.getStartTime().plus(ev.getDuration())), "end == start + duration");
            // thread semantics: committed on the current thread
            RecordedThread th = ev.getThread();
            check(th != null, "thread non-null");
            String mainName = Thread.currentThread().getName();
            check(mainName.equals(th.getJavaName()), "committed thread javaName == current thread name");
            // stack trace semantics: withStackTrace -> present, withoutStackTrace -> absent
            check(ev.getStackTrace() != null, "withStackTrace -> stack trace recorded");
            check(ev.getStackTrace().getFrames() != null && !ev.getStackTrace().getFrames().isEmpty(), "stack frames non-empty");
            RecordedEvent ns = find("test.RecordedEvent.NoStack");
            check(ns != null, "found no-stack event");
            check(ns.getStackTrace() == null, "withoutStackTrace -> null stack trace");
            check(ns.getLong("v") == 7L, "no-stack event field intact");
            // fields agree with EventType metadata (same name set)
            Set<String> fromEvent = new TreeSet<>(), fromType = new TreeSet<>();
            for (ValueDescriptor vd : ev.getFields()) fromEvent.add(vd.getName());
            for (ValueDescriptor vd : t.getFields()) fromType.add(vd.getName());
            check(fromEvent.equals(fromType), "getFields names == EventType.getFields names");
            check(fromType.contains("message"), "fields contain user field");
            check(fromType.contains("startTime") && fromType.contains("duration") && fromType.contains("eventThread") && fromType.contains("stackTrace"), "implicit fields present");
            // typed access + hasField
            check(ev.hasField("message"), "has message field");
            check("hello".equals(ev.getString("message")), "message value");
            check(!ev.hasField("nope"), "hasField unknown false");
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

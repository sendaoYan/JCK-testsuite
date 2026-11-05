/*
 * @test
 * @summary jdk.jfr.consumer.RecordedEvent conformance: structure + metadata/time/thread/stack contract.
 * @run main RecordedEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedEvent", false, RecordedEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.jfr.consumer.RecordedObject"), "superclass jdk.jfr.consumer.RecordedObject");
        check(hasMethod(c, "getDuration"), "declares getDuration");
        check(hasMethod(c, "getEndTime"), "declares getEndTime");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getStackTrace"), "declares getStackTrace");
        check(hasMethod(c, "getStartTime"), "declares getStartTime");
        check(hasMethod(c, "getThread"), "declares getThread");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        Path file = Files.createTempFile("re8", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable(E.class).withStackTrace();
                r.enable(NoStack.class).withoutStackTrace();
                r.start();
                E ev = new E(); ev.message = "hello"; ev.commit();
                NoStack ns = new NoStack(); ns.v = 7; ns.commit();
                r.stop();
                r.dump(file);
            }
            RecordedEvent ev = null, ns = null;
            for (RecordedEvent e : RecordingFile.readAllEvents(file)) {
                if ("test.RecordedEvent.E".equals(e.getEventType().getName())) ev = e;
                if ("test.RecordedEvent.NoStack".equals(e.getEventType().getName())) ns = e;
            }
            check(ev != null, "found custom event");
            // metadata channel
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
            // thread semantics
            RecordedThread th = ev.getThread();
            check(th != null, "thread non-null");
            check(Thread.currentThread().getName().equals(th.getJavaName()), "committed thread javaName == current thread name");
            // stack trace semantics
            check(ev.getStackTrace() != null, "withStackTrace -> stack trace recorded");
            check(ev.getStackTrace().getFrames() != null && !ev.getStackTrace().getFrames().isEmpty(), "stack frames non-empty");
            check(ns != null, "found no-stack event");
            check(ns.getStackTrace() == null, "withoutStackTrace -> null stack trace");
            check(ns.getLong("v") == 7L, "no-stack event field intact");
            // fields agree with EventType metadata
            Set<String> fromEvent = new TreeSet<>(), fromType = new TreeSet<>();
            for (ValueDescriptor vd : ev.getFields()) fromEvent.add(vd.getName());
            for (ValueDescriptor vd : t.getFields()) fromType.add(vd.getName());
            check(fromEvent.equals(fromType), "getFields names == EventType.getFields names");
            check(fromType.contains("message"), "fields contain user field");
            check(fromType.contains("startTime") && fromType.contains("duration") && fromType.contains("eventThread") && fromType.contains("stackTrace"), "implicit fields present");
            check(ev.hasField("message"), "has message field");
            check("hello".equals(ev.getString("message")), "message value");
            check(!ev.hasField("nope"), "hasField unknown false");
        } finally {
            Files.deleteIfExists(file);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

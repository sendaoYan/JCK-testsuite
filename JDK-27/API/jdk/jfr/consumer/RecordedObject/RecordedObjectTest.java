/*
 * @test
 * @summary jdk.jfr.consumer.RecordedObject conformance: white-box structure + black-box getValue.
 * @run main RecordedObjectTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

public class RecordedObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ConsumerEvent")
    @Label("Consumer Event")
    static class ConsumerEvent extends Event {
        @Label("message") String message;
    }

    @Name("test.RecordedObject.E")
    @Label("Recorded Object Event")
    static class E extends Event {
        @Label("b") boolean b = true;
        @Label("y") byte y = 3;
        @Label("ch") char ch = 'x';
        @Label("sh") short sh = 7;
        @Label("i") int i = 11;
        @Label("l") long l = 123456789012L;
        @Label("f") float f = 1.5f;
        @Label("d") double d = 2.5;
        @Label("s") String s = "str";
        @Label("ns") String ns = null;
        @Label("thr") Thread thr;
        @Label("cls") Class<?> cls;
    }

    static RecordedEvent ev;

    // ---- original black-box smoke (kept verbatim) ----
    static void testBlackBoxOriginal() throws Exception {
        Path p = Files.createTempFile("ro", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(ConsumerEvent.class);
            r.start();
            ConsumerEvent c = new ConsumerEvent(); c.message = "hello"; c.commit();
            r.stop();
            r.dump(p);
        }
        RecordedEvent ev = null;
        try (RecordingFile rf = new RecordingFile(p)) {
            while (rf.hasMoreEvents()) {
                RecordedEvent e = rf.readEvent();
                if ("com.test.ConsumerEvent".equals(e.getEventType().getName())) { ev = e; break; }
            }
        }
        Files.deleteIfExists(p);
        check(ev != null, "found event");
        RecordedObject o = ev;
        check(o.hasField("message"), "hasField");
        check("hello".equals(o.getValue("message")), "getValue");
        check(o.getFields() != null, "getFields");
        check(o.toString() != null, "toString");
    }

    static void recordFixture() throws Exception {
        Path p = Files.createTempFile("rot", ".jfr");
        try (Recording r = new Recording()) {
            r.enable(E.class).withStackTrace();
            r.start();
            E e = new E();
            e.thr = Thread.currentThread();
            e.cls = RecordedObjectTest.class;
            e.commit();
            r.stop();
            r.dump(p);
        }
        for (RecordedEvent cand : RecordingFile.readAllEvents(p)) {
            if (cand.getEventType().getName().equals("test.RecordedObject.E")) { ev = cand; break; }
        }
        Files.deleteIfExists(p);
    }

    static Throwable caught(Runnable r) {
        try { r.run(); return null; } catch (Throwable t) { return t; }
    }

    static void checkIae(Throwable t, String contains, String what) {
        check(t instanceof IllegalArgumentException, what + " -> IllegalArgumentException (got " + t + ")");
        check(t.getMessage() != null && t.getMessage().contains(contains), what + " message contains \"" + contains + "\" (was: " + t.getMessage() + ")");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedObject");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getValue"), "getValue");
        check(hasMethod(c, "hasField"), "hasField");
        check(hasMethod(c, "getFields"), "getFields");
        check(hasMethod(c, "getBoolean"), "getBoolean");
        check(hasMethod(c, "getByte"), "getByte");
        check(hasMethod(c, "getChar"), "getChar");
        check(hasMethod(c, "getShort"), "getShort");
        check(hasMethod(c, "getInt"), "getInt");
        check(hasMethod(c, "getLong"), "getLong");
        check(hasMethod(c, "getFloat"), "getFloat");
        check(hasMethod(c, "getDouble"), "getDouble");
        check(hasMethod(c, "getString"), "getString");
        check(hasMethod(c, "getDuration"), "getDuration");
        check(hasMethod(c, "getInstant"), "getInstant");
        check(hasMethod(c, "getClass"), "getClass(String)");
        check(hasMethod(c, "getThread"), "getThread(String)");
    }

    static void testTypedReads() throws Exception {
        check(ev.getBoolean("b") == true, "getBoolean");
        check(ev.getByte("y") == 3, "getByte");
        check(ev.getChar("ch") == 'x', "getChar");
        check(ev.getShort("sh") == 7, "getShort");
        check(ev.getInt("i") == 11, "getInt");
        check(ev.getLong("l") == 123456789012L, "getLong");
        check(ev.getFloat("f") == 1.5f, "getFloat");
        check(ev.getDouble("d") == 2.5, "getDouble");
        check("str".equals(ev.getString("s")), "getString");
        check(ev.getString("ns") == null, "getString on null-valued field -> null");
        // widening primitive conversion int -> long is legal
        check(ev.getLong("i") == 11L, "getLong on int field widens");
        check((Object) ev.getValue("b") instanceof Boolean, "getValue boxes boolean");
        check((Object) ev.getValue("l") instanceof Long, "getValue boxes long");
        check("str".equals(ev.getValue("s")), "getValue String");
        check(ev.getValue("thr") instanceof RecordedThread, "getValue Thread field -> RecordedThread");
        check(ev.getValue("cls") instanceof RecordedClass, "getValue Class field -> RecordedClass");
        check(ev.getThread("thr") != null, "getThread(String)");
        check(ev.getClass("cls") != null, "getClass(String)");
        // hasField
        check(ev.hasField("b") && ev.hasField("l") && ev.hasField("s") && ev.hasField("thr") && ev.hasField("cls"), "hasField true for existing");
        check(!ev.hasField("nope"), "hasField false for unknown");
        check(ev.hasField("eventThread") && ev.hasField("startTime"), "implicit fields visible to hasField");
        // getFields names
        Set<String> names = new HashSet<>();
        for (ValueDescriptor vd : ev.getFields()) names.add(vd.getName());
        for (String n : new String[]{"b", "y", "ch", "sh", "i", "l", "f", "d", "s", "ns", "thr", "cls"}) {
            check(names.contains(n), "getFields contains " + n);
        }
        // toString mentions a field name (weak)
        check(ev.toString() != null && ev.toString().contains("s"), "toString non-null and mentions field");
    }

    static void testMismatchMatrix() throws Exception {
        // missing field -> IllegalArgumentException "Could not find field with name <name>"
        checkIae(caught(() -> ev.getValue("nope")), "Could not find field with name nope", "getValue(nope)");
        checkIae(caught(() -> ev.getLong("nope")), "Could not find field with name nope", "getLong(nope)");
        checkIae(caught(() -> ev.getBoolean("nope")), "Could not find field with name nope", "getBoolean(nope)");
        checkIae(caught(() -> ev.getString("nope")), "nope", "getString(nope)");
        // type mismatches -> IllegalArgumentException mentioning the field name
        checkIae(caught(() -> ev.getLong("s")), "s", "getLong on String field");
        checkIae(caught(() -> ev.getString("l")), "l", "getString on long field");
        checkIae(caught(() -> ev.getBoolean("s")), "s", "getBoolean on String field");
        checkIae(caught(() -> ev.getDouble("s")), "s", "getDouble on String field");
        checkIae(caught(() -> ev.getFloat("s")), "s", "getFloat on String field");
        checkIae(caught(() -> ev.getChar("s")), "s", "getChar on String field");
        // narrowing long -> int is illegal
        checkIae(caught(() -> ev.getInt("l")), "l", "getInt on long field (narrowing)");
        // narrowing int -> byte/short is illegal
        checkIae(caught(() -> ev.getByte("i")), "i", "getByte on int field");
        checkIae(caught(() -> ev.getShort("i")), "i", "getShort on int field");
        // object-type mismatches
        checkIae(caught(() -> ev.getClass("l")), "l", "getClass on long field");
        checkIae(caught(() -> ev.getClass("b")), "b", "getClass on boolean field");
        checkIae(caught(() -> ev.getThread("s")), "s", "getThread on String field");
        checkIae(caught(() -> ev.getDuration("s")), "Duration", "getDuration on String field");
        checkIae(caught(() -> ev.getInstant("s")), "Instant", "getInstant on String field");
        // long field without @Timestamp cannot be read as Instant
        checkIae(caught(() -> ev.getInstant("l")), "l", "getInstant on long field without @Timestamp");
        // RecordedThread field cannot be read as primitive
        checkIae(caught(() -> ev.getLong("thr")), "thr", "getLong on Thread field");
        // null name -> NPE
        Throwable npe = caught(() -> ev.getValue(null));
        check(npe instanceof NullPointerException, "getValue(null) -> NPE");
        Throwable npe2 = caught(() -> ev.hasField(null));
        check(npe2 instanceof NullPointerException, "hasField(null) -> NPE");
    }

    static void testImplicitFields() throws Exception {
        // startTime is a raw long via getValue (ticks), getInstant needs @Timestamp fields
        Object st = ev.getValue("startTime");
        check(st instanceof Long, "getValue(startTime) -> Long ticks");
        check((Long) st > 0L, "startTime ticks > 0");
        check(ev.getValue("duration") instanceof Long, "getValue(duration) -> Long");
        check(ev.getValue("eventThread") instanceof RecordedThread, "getValue(eventThread) -> RecordedThread");
        check(ev.getThread("eventThread") != null, "getThread(eventThread)");
        check(ev.getValue("stackTrace") instanceof RecordedStackTrace, "getValue(stackTrace) -> RecordedStackTrace");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        recordFixture();
        check(ev != null, "recorded fixture event found");
        try {
            testTypedReads();
            testMismatchMatrix();
            testImplicitFields();
        } finally {
            ev = null;
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

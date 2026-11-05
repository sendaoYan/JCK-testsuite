/*
 * @test
 * @summary jdk.jfr.consumer.RecordedObject conformance: structure + typed getters + type-mismatch matrix.
 * @run main RecordedObjectTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import jdk.jfr.*;
import jdk.jfr.consumer.*;

public class RecordedObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }

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

    static Throwable caught(Runnable r) {
        try { r.run(); return null; } catch (Throwable t) { return t; }
    }

    static void checkIae(Throwable t, String contains, String what) {
        check(t instanceof IllegalArgumentException, what + " -> IllegalArgumentException (got " + t + ")");
        check(t.getMessage() != null && t.getMessage().contains(contains), what + " message contains \"" + contains + "\" (was: " + t.getMessage() + ")");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.RecordedObject", false, RecordedObjectTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getBoolean"), "declares getBoolean");
        check(hasMethod(c, "getByte"), "declares getByte");
        check(hasMethod(c, "getChar"), "declares getChar");
        check(hasMethod(c, "getClass"), "declares getClass");
        check(hasMethod(c, "getDouble"), "declares getDouble");
        check(hasMethod(c, "getDuration"), "declares getDuration");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getFloat"), "declares getFloat");

        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); System.out.println("PASS (" + passed + " assertions)"); return; }

        Path p = Files.createTempFile("ro8", ".jfr");
        try {
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
            check(ev != null, "recorded fixture event found");

            // typed reads
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
            check(ev.getLong("i") == 11L, "getLong on int field widens");
            check((Object) ev.getValue("b") instanceof Boolean, "getValue boxes boolean");
            check((Object) ev.getValue("l") instanceof Long, "getValue boxes long");
            check("str".equals(ev.getValue("s")), "getValue String");
            check(ev.getValue("thr") instanceof RecordedThread, "getValue Thread field -> RecordedThread");
            check(ev.getValue("cls") instanceof RecordedClass, "getValue Class field -> RecordedClass");
            check(ev.getThread("thr") != null, "getThread(String)");
            check(ev.getClass("cls") != null, "getClass(String)");
            check(ev.hasField("b") && ev.hasField("l") && ev.hasField("s") && ev.hasField("thr") && ev.hasField("cls"), "hasField true for existing");
            check(!ev.hasField("nope"), "hasField false for unknown");
            check(ev.hasField("eventThread") && ev.hasField("startTime"), "implicit fields visible to hasField");
            Set<String> names = new HashSet<>();
            for (ValueDescriptor vd : ev.getFields()) names.add(vd.getName());
            for (String n : new String[]{"b", "y", "ch", "sh", "i", "l", "f", "d", "s", "ns", "thr", "cls"}) {
                check(names.contains(n), "getFields contains " + n);
            }
            check(ev.toString() != null && ev.toString().contains("s"), "toString non-null and mentions field");

            // missing field -> IAE "Could not find field with name <name>"
            checkIae(caught(() -> ev.getValue("nope")), "Could not find field with name nope", "getValue(nope)");
            checkIae(caught(() -> ev.getLong("nope")), "Could not find field with name nope", "getLong(nope)");
            checkIae(caught(() -> ev.getBoolean("nope")), "Could not find field with name nope", "getBoolean(nope)");
            checkIae(caught(() -> ev.getString("nope")), "nope", "getString(nope)");
            // type mismatches
            checkIae(caught(() -> ev.getLong("s")), "s", "getLong on String field");
            checkIae(caught(() -> ev.getString("l")), "l", "getString on long field");
            checkIae(caught(() -> ev.getBoolean("s")), "s", "getBoolean on String field");
            checkIae(caught(() -> ev.getDouble("s")), "s", "getDouble on String field");
            checkIae(caught(() -> ev.getFloat("s")), "s", "getFloat on String field");
            checkIae(caught(() -> ev.getChar("s")), "s", "getChar on String field");
            checkIae(caught(() -> ev.getInt("l")), "l", "getInt on long field (narrowing)");
            checkIae(caught(() -> ev.getByte("i")), "i", "getByte on int field");
            checkIae(caught(() -> ev.getShort("i")), "i", "getShort on int field");
            checkIae(caught(() -> ev.getClass("l")), "l", "getClass on long field");
            checkIae(caught(() -> ev.getClass("b")), "b", "getClass on boolean field");
            checkIae(caught(() -> ev.getThread("s")), "s", "getThread on String field");
            checkIae(caught(() -> ev.getDuration("s")), "Duration", "getDuration on String field");
            checkIae(caught(() -> ev.getInstant("s")), "Instant", "getInstant on String field");
            checkIae(caught(() -> ev.getInstant("l")), "l", "getInstant on long field without @Timestamp");
            checkIae(caught(() -> ev.getLong("thr")), "thr", "getLong on Thread field");
            // null name -> NPE
            check(caught(() -> ev.getValue(null)) instanceof NullPointerException, "getValue(null) -> NPE");
            check(caught(() -> ev.hasField(null)) instanceof NullPointerException, "hasField(null) -> NPE");

            // implicit fields
            check(ev.getValue("startTime") instanceof Long, "getValue(startTime) -> Long ticks");
            check((Long) ev.getValue("startTime") > 0L, "startTime ticks > 0");
            check(ev.getValue("eventThread") instanceof RecordedThread, "getValue(eventThread) -> RecordedThread");
            check(ev.getValue("stackTrace") instanceof RecordedStackTrace, "getValue(stackTrace) -> RecordedStackTrace");
        } finally {
            Files.deleteIfExists(p);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}

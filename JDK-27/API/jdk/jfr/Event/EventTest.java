/*
 * @test
 * @summary jdk.jfr.Event conformance: white-box structure + black-box commit lifecycle.
 * @run main EventTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class EventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.BeanEvent")
    @Label("Test Event")
    @Description("A test event")
    static class TestEvent extends Event {
        @Label("message") String message;
        @Label("count") int count;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Event");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.isAssignableFrom(TestEvent.class) || Event.class.isAssignableFrom(TestEvent.class), "TestEvent extends Event");
        check(hasMethod(c, "begin"), "begin");
        check(hasMethod(c, "end"), "end");
        check(hasMethod(c, "commit"), "commit");
        check(hasMethod(c, "shouldCommit"), "shouldCommit");
        check(hasMethod(c, "isEnabled"), "isEnabled");
        // JDK-27: Event extends jdk.internal.event.Event (shared base with jdk.internal.event infrastructure)
        check("jdk.internal.event.Event".equals(c.getSuperclass().getName()), "superclass jdk.internal.event.Event");
    }

    static void testBlackBox() {
        TestEvent e = new TestEvent();
        e.begin();
        e.message = "hello";
        e.count = 42;
        e.end();
        check(e.shouldCommit() || !e.shouldCommit(), "shouldCommit callable");
        e.commit();
        check(true, "committed without exception");
        check(TestEvent.class.getAnnotation(Name.class) != null, "Name annotation present");
        check("com.test.BeanEvent".equals(TestEvent.class.getAnnotation(Name.class).value()), "Name value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepAnnotationsAndFields();
        testDeepCommitWithoutRecording();
        testDeepLifecycleLoop();
        testDeepSetIndex();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static void expectEx(String what, Class<? extends Throwable> type, String contains, Runnable r) {
        try { r.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }

    @Name("test.Event.TEv") @Label("TE") @Category({"ec"}) @Description("ed")
    @Enabled(false) @Threshold("1 ms") @StackTrace(false)
    static class TEv extends Event {
        @Label("id") long id;
        @Label("msg") String msg;
        @Label("d") double d;
        @Label("b") boolean b;
        @Label("i") int i;
    }

    static void testDeepAnnotationsAndFields() throws Exception {
        EventType t = EventType.getEventType(TEv.class);
        check("test.Event.TEv".equals(t.getName()), "ev name");
        check("TE".equals(t.getLabel()), "ev label");
        check("ed".equals(t.getDescription()), "ev description");
        check(t.getCategoryNames().size() == 1 && "ec".equals(t.getCategoryNames().get(0)), "ev category");
        check(t.getAnnotation(Name.class) != null && "test.Event.TEv".equals(t.getAnnotation(Name.class).value()), "ev @Name roundtrip");
        check(t.getAnnotation(Label.class) != null && "TE".equals(t.getAnnotation(Label.class).value()), "ev @Label roundtrip");
        check(t.getAnnotation(Enabled.class) == null, "ev @Enabled is setting-derived, not returned by getAnnotation");
        check(t.getAnnotation(Threshold.class) == null, "ev @Threshold is setting-derived, not returned by getAnnotation");
        check(t.getAnnotation(StackTrace.class) == null, "ev @StackTrace is setting-derived, not returned by getAnnotation");
        Map<String, String> defs = new HashMap<String, String>();
        for (SettingDescriptor sd : t.getSettingDescriptors()) defs.put(sd.getName(), sd.getDefaultValue());
        check("false".equals(defs.get("enabled")), "ev setting enabled default false from @Enabled(false)");
        check("1 ms".equals(defs.get("threshold")), "ev setting threshold default '1 ms' from @Threshold");
        check("false".equals(defs.get("stackTrace")), "ev setting stackTrace default false from @StackTrace(false)");
        check(t.getId() > 0, "ev id > 0");
        List<String> fieldNames = new ArrayList<String>();
        for (ValueDescriptor vd : t.getFields()) fieldNames.add(vd.getName());
        check(fieldNames.size() >= 9, "ev field count >= implicit 4 + user 5, got " + fieldNames.size());
        check("startTime".equals(fieldNames.get(0)) && "duration".equals(fieldNames.get(1))
              && "eventThread".equals(fieldNames.get(2)) && "stackTrace".equals(fieldNames.get(3)), "ev implicit field order");
        check(fieldNames.contains("id") && fieldNames.contains("msg") && fieldNames.contains("d")
              && fieldNames.contains("b") && fieldNames.contains("i"), "ev user fields present");
        check(t.getField("id") != null && "id".equals(t.getField("id").getName()), "ev getField(id)");
        check("id".equals(t.getField("id").getLabel()), "ev field label via @Label");
        check(t.getField("nope") == null, "ev getField(missing) null");
    }

    static void testDeepCommitWithoutRecording() {
        TEv e = new TEv();
        check(!e.isEnabled(), "isEnabled false with no active recording");
        check(!e.shouldCommit(), "shouldCommit false with no active recording");
        e.begin();
        e.end();
        e.commit();
        check(true, "commit without recording is silent");
        TEv e2 = new TEv();
        e2.begin();
        e2.commit();
        check(true, "commit without end() tolerated");
    }

    static void testDeepLifecycleLoop() throws Exception {
        Path tmp = Files.createTempFile("ev-loop", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable("test.Event.TEv").withThreshold(Duration.ZERO);
                r.start();
                TEv e = new TEv();
                check(e.isEnabled(), "isEnabled true while enabled+running");
                check(e.shouldCommit(), "shouldCommit true while enabled+running (threshold 0)");
                e.begin();
                e.id = 42; e.msg = "hello"; e.d = 3.5; e.b = true; e.i = -7;
                e.end();
                e.commit();
                TEv e2 = new TEv();
                e2.id = 43; e2.msg = "world"; e2.d = -0.25; e2.b = false; e2.i = 99;
                e2.commit(); // no begin/end -> duration 0
                r.stop();
                r.dump(tmp);
            }
            List<RecordedEvent> mine = new ArrayList<RecordedEvent>();
            for (RecordedEvent re : RecordingFile.readAllEvents(tmp)) {
                if (re.getEventType().getName().equals("test.Event.TEv")) mine.add(re);
            }
            check(mine.size() == 2, "ev loop 2 events, got " + mine.size());
            RecordedEvent a = mine.get(0), b = mine.get(1);
            check(a.getLong("id") == 42L && b.getLong("id") == 43L, "ev loop ids order 42,43");
            check("hello".equals(a.getString("msg")) && "world".equals(b.getString("msg")), "ev loop msg roundtrip");
            check(a.getDouble("d") == 3.5 && b.getDouble("d") == -0.25, "ev loop double roundtrip");
            check(a.getBoolean("b") == true && b.getBoolean("b") == false, "ev loop boolean roundtrip");
            check(a.getInt("i") == -7 && b.getInt("i") == 99, "ev loop int roundtrip");
            check(!a.getDuration().isNegative(), "ev loop duration begin/end >= 0");
            check(b.getDuration().equals(Duration.ZERO) || !b.getDuration().isNegative(), "ev loop no-begin duration >= 0");
            check(a.getStartTime().compareTo(a.getEndTime()) <= 0, "ev loop start<=end");
            check(a.getThread() != null, "ev loop thread recorded");
            check(a.getStackTrace() == null, "ev loop stackTrace null because @StackTrace(false)");
        } finally { Files.deleteIfExists(tmp); }
    }

    static void testDeepSetIndex() {
        TEv e = new TEv();
        e.set(0, 42L);
        e.set(1, "s");
        check(true, "set() in-range with matching types does not throw");
        // Empirical (8+27 identical): set() on a statically compiled Event subclass does NOT
        // bounds-check the index (unlike EventFactory-generated events, which throw IOOBE).
        try { e.set(-1, 1L); check(true, "set(-1) tolerated on compiled event"); }
        catch (Throwable t) { check(false, "set(-1) should be tolerated on compiled event, got " + t); }
        try { e.set(99, 1L); check(true, "set(99) tolerated on compiled event"); }
        catch (Throwable t) { check(false, "set(99) should be tolerated on compiled event, got " + t); }
        try { e.set(0, "not-a-long"); check(true, "set() with mismatched type does not throw at set-time"); }
        catch (Throwable t) { check(false, "set() with mismatched type should not throw at set-time, got " + t); }
        // do not commit e after the mismatched set above
    }
}

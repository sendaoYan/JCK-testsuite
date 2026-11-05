/*
 * @test
 * @summary jdk.jfr.RecordingState conformance: white-box structure + black-box enum behavior.
 * @run main RecordingStateTest
 */

import java.lang.reflect.*;
import jdk.jfr.RecordingState;

public class RecordingStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.RecordingState");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "isEnum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 5, "5 enum constants");
        check(RecordingState.NEW != null && RecordingState.DELAYED != null
              && RecordingState.RUNNING != null && RecordingState.STOPPED != null
              && RecordingState.CLOSED != null, "constants present");
    }

    static void testBlackBox() {
        check(RecordingState.valueOf("NEW") == RecordingState.NEW, "valueOf NEW");
        check(RecordingState.valueOf("CLOSED") == RecordingState.CLOSED, "valueOf CLOSED");
        check(RecordingState.values().length == 5, "values length");
        check(RecordingState.NEW.ordinal() == 0, "NEW ordinal 0");
        check(RecordingState.CLOSED.ordinal() == 4, "CLOSED ordinal 4");
        check(RecordingState.NEW.ordinal() < RecordingState.DELAYED.ordinal()
              && RecordingState.DELAYED.ordinal() < RecordingState.RUNNING.ordinal()
              && RecordingState.RUNNING.ordinal() < RecordingState.STOPPED.ordinal()
              && RecordingState.STOPPED.ordinal() < RecordingState.CLOSED.ordinal(), "ordinal ordering");
        check(RecordingState.NEW.toString().equals("NEW"), "toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepOrdering();
        testDeepValueOf();
        testDeepEnumContract();
        testDeepLifecycleOrdering();
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

    static void testDeepOrdering() {
        RecordingState[] vals = RecordingState.values();
        check(vals.length == 5, "rs exactly 5 constants, got " + vals.length);
        check(vals[0] == RecordingState.NEW && vals[1] == RecordingState.DELAYED && vals[2] == RecordingState.RUNNING
              && vals[3] == RecordingState.STOPPED && vals[4] == RecordingState.CLOSED, "rs order NEW,DELAYED,RUNNING,STOPPED,CLOSED");
        check(RecordingState.NEW.ordinal() == 0, "rs NEW ordinal 0");
        check(RecordingState.DELAYED.ordinal() == 1, "rs DELAYED ordinal 1");
        check(RecordingState.RUNNING.ordinal() == 2, "rs RUNNING ordinal 2");
        check(RecordingState.STOPPED.ordinal() == 3, "rs STOPPED ordinal 3");
        check(RecordingState.CLOSED.ordinal() == 4, "rs CLOSED ordinal 4");
        check("NEW".equals(RecordingState.NEW.name()), "rs name NEW");
        check("DELAYED".equals(RecordingState.DELAYED.name()), "rs name DELAYED");
        check("RUNNING".equals(RecordingState.RUNNING.name()), "rs name RUNNING");
        check("STOPPED".equals(RecordingState.STOPPED.name()), "rs name STOPPED");
        check("CLOSED".equals(RecordingState.CLOSED.name()), "rs name CLOSED");
    }

    static void testDeepValueOf() {
        check(RecordingState.valueOf("NEW") == RecordingState.NEW, "rs valueOf NEW");
        check(RecordingState.valueOf("DELAYED") == RecordingState.DELAYED, "rs valueOf DELAYED");
        check(RecordingState.valueOf("RUNNING") == RecordingState.RUNNING, "rs valueOf RUNNING");
        check(RecordingState.valueOf("STOPPED") == RecordingState.STOPPED, "rs valueOf STOPPED");
        check(RecordingState.valueOf("CLOSED") == RecordingState.CLOSED, "rs valueOf CLOSED");
        expectEx("rs valueOf(bogus)", IllegalArgumentException.class, null, () -> RecordingState.valueOf("bogus"));
        expectEx("rs valueOf(null)", NullPointerException.class, null, () -> RecordingState.valueOf(null));
        // Enum.valueOf generic channel agrees with the class-specific valueOf
        check(Enum.valueOf(RecordingState.class, "RUNNING") == RecordingState.RUNNING, "rs Enum.valueOf channel");
    }

    static void testDeepEnumContract() {
        check(RecordingState.NEW.compareTo(RecordingState.DELAYED) < 0, "rs NEW < DELAYED");
        check(RecordingState.DELAYED.compareTo(RecordingState.RUNNING) < 0, "rs DELAYED < RUNNING");
        check(RecordingState.RUNNING.compareTo(RecordingState.STOPPED) < 0, "rs RUNNING < STOPPED");
        check(RecordingState.STOPPED.compareTo(RecordingState.CLOSED) < 0, "rs STOPPED < CLOSED");
        check(RecordingState.RUNNING.compareTo(RecordingState.RUNNING) == 0, "rs compareTo self == 0");
        check(RecordingState.CLOSED.compareTo(RecordingState.NEW) > 0, "rs CLOSED > NEW");
        check("NEW".equals(RecordingState.NEW.toString()), "rs toString NEW");
        check(RecordingState.NEW.getDeclaringClass() == RecordingState.class, "rs declaringClass");
        // values() returns a fresh array each call
        RecordingState[] a1 = RecordingState.values();
        RecordingState[] a2 = RecordingState.values();
        check(a1 != a2, "rs values() returns fresh array");
        a1[0] = RecordingState.CLOSED;
        check(RecordingState.values()[0] == RecordingState.NEW, "rs mutating returned array does not affect enum");
    }

    static void testDeepLifecycleOrdering() throws Exception {
        // the enum order matches the reachable state machine transitions of a real recording
        jdk.jfr.Recording r = new jdk.jfr.Recording();
        try {
            check(r.getState() == RecordingState.NEW, "rs lifecycle starts at NEW");
            r.start();
            check(r.getState() == RecordingState.RUNNING, "rs lifecycle RUNNING after start");
            r.stop();
            check(r.getState() == RecordingState.STOPPED, "rs lifecycle STOPPED after stop");
        } finally { r.close(); }
        check(r.getState() == RecordingState.CLOSED, "rs lifecycle CLOSED after close");
    }
}

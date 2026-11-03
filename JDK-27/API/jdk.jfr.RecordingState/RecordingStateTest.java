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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.sound.midi.Sequencer.SyncMode conformance: white-box structure + black-box behavior.
 * @run main SyncModeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Sequencer;

public class SyncModeTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Sequencer$SyncMode");
        check(!c.isInterface(), "is not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "is not abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> fields = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getFields()) fields.add(f.getName());
        check(fields.contains("INTERNAL_CLOCK"), "field INTERNAL_CLOCK");
        check(fields.contains("MIDI_SYNC"), "field MIDI_SYNC");
        check(fields.contains("MIDI_TIME_CODE"), "field MIDI_TIME_CODE");
        check(fields.contains("NO_SYNC"), "field NO_SYNC");
        for (String n : new String[]{"equals", "hashCode", "toString"}) {
            java.lang.reflect.Method m = c.getDeclaredMethod(n, n.equals("equals") ? new Class[]{Object.class} : new Class[0]);
            check(Modifier.isFinal(m.getModifiers()), n + " is final");
        }
    }

    static void testBlackBox() throws Exception {
        Sequencer.SyncMode custom = new SyncImpl("Custom Mode");
        check("Custom Mode".equals(custom.toString()), "toString returns name");
        check(custom.equals(custom), "equals reflexive");
        check(custom.hashCode() == custom.hashCode(), "hashCode stable");
        SyncImpl other = new SyncImpl("Custom Mode");
        check(!custom.equals(other), "equals identity-based (distinct false)");

        Sequencer.SyncMode ic = Sequencer.SyncMode.INTERNAL_CLOCK;
        Sequencer.SyncMode ms = Sequencer.SyncMode.MIDI_SYNC;
        Sequencer.SyncMode mtc = Sequencer.SyncMode.MIDI_TIME_CODE;
        Sequencer.SyncMode ns = Sequencer.SyncMode.NO_SYNC;
        check("Internal Clock".equals(ic.toString()), "INTERNAL_CLOCK toString");
        check("MIDI Sync".equals(ms.toString()), "MIDI_SYNC toString");
        check("MIDI Time Code".equals(mtc.toString()), "MIDI_TIME_CODE toString");
        check("No Timing".equals(ns.toString()), "NO_SYNC toString");
        check(!ic.equals(ms), "built-in constants are distinct instances");
        check(ic == Sequencer.SyncMode.INTERNAL_CLOCK, "constant is a singleton");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class SyncImpl extends Sequencer.SyncMode {
    SyncImpl(String n) { super(n); }
}

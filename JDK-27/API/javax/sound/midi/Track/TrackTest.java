/*
 * @test
 * @summary javax.sound.midi.Track conformance: white-box structure + black-box behavior.
 * @run main TrackTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class TrackTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()), msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Track");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) methods.add(m.getName());
        for (String n : new String[]{"add", "remove", "get", "size", "ticks"}) check(methods.contains(n), "public method " + n);
    }

    static void testBlackBox() throws Exception {
        Sequence s = new Sequence(Sequence.PPQ, 96);
        Track t = s.createTrack();
        check(t.size() == 1, "new track has single end-of-track event");
        check(t.ticks() == 0L, "empty track ticks 0");

        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
        MidiEvent ev = new MidiEvent(sm, 10L);
        check(t.add(ev), "add returns true");
        check(t.size() == 2, "size 2 after add");
        check(t.get(0).getMessage() == sm, "get(0) is added event");
        check(t.get(0).getTick() == 10L, "event tick preserved");
        check(t.ticks() == 10L, "ticks reflects last event tick");

        ShortMessage sm2 = new ShortMessage();
        sm2.setMessage(ShortMessage.NOTE_ON, 0, 61, 100);
        MidiEvent ev2 = new MidiEvent(sm2, 5L);
        check(t.add(ev2), "add earlier event");
        check(t.get(0).getMessage() == sm2, "earlier event inserted first");
        check(t.get(1).getMessage() == sm, "later event follows");

        check(!t.add(ev), "duplicate add returns false");
        check(t.size() == 3, "duplicate not added");

        check(t.remove(ev), "remove returns true");
        check(!t.remove(ev), "remove absent returns false");
        check(!t.add(null), "add null returns false");
        check(!t.remove(null), "remove null returns false");

        expectThrows(ArrayIndexOutOfBoundsException.class, () -> t.get(-1), "get negative index");
        expectThrows(ArrayIndexOutOfBoundsException.class, () -> t.get(t.size()), "get index == size");

        Track t2 = new Sequence(Sequence.PPQ, 96).createTrack();
        while (t2.size() > 0) t2.remove(t2.get(0));
        check(t2.size() == 0, "removing all incl EOT empties track");
        ShortMessage sm3 = new ShortMessage();
        sm3.setMessage(ShortMessage.NOTE_ON, 0, 62, 100);
        check(t2.add(new MidiEvent(sm3, 7L)), "add after emptying");
        check(t2.size() == 2, "EOT re-established after add");
        check(t2.ticks() == 7L, "ticks after re-add");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

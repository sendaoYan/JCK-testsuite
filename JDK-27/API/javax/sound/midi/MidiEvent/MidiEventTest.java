/*
 * @test
 * @summary javax.sound.midi.MidiEvent conformance: white-box structure + black-box behavior.
 * @run main MidiEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class MidiEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("getMessage"), "method getMessage");
        check(dm.contains("setTick"), "method setTick");
        check(dm.contains("getTick"), "method getTick");
        // message field is final; tick field is mutable.
        boolean messageFinal = false;
        boolean tickMutable = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("message")) messageFinal = Modifier.isFinal(f.getModifiers());
            if (f.getName().equals("tick")) tickMutable = !Modifier.isFinal(f.getModifiers());
        }
        check(messageFinal, "message field final");
        check(tickMutable, "tick field mutable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);

        MidiEvent ev = new MidiEvent(sm, 10L);
        check(ev.getMessage() == sm, "getMessage returns same message instance");
        check(ev.getTick() == 10L, "getTick initial");

        ev.setTick(20L);
        check(ev.getTick() == 20L, "setTick round-trip");

        ev.setTick(0L);
        check(ev.getTick() == 0L, "setTick zero");
        ev.setTick(-5L);
        check(ev.getTick() == -5L, "setTick negative");
        ev.setTick(Long.MAX_VALUE);
        check(ev.getTick() == Long.MAX_VALUE, "setTick max");

        MetaMessage mm = new MetaMessage(0x2F, new byte[]{0x01}, 1);
        MidiEvent ev2 = new MidiEvent(mm, 480L);
        check(ev2.getMessage() == mm, "meta message identity");
        check(ev2.getTick() == 480L, "meta event tick");

        // Two events over the same message are independent in tick.
        MidiEvent a = new MidiEvent(sm, 1L);
        MidiEvent b = new MidiEvent(sm, 2L);
        a.setTick(100L);
        check(a.getTick() == 100L && b.getTick() == 2L, "independent tick state");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.sound.midi.ShortMessage conformance: white-box structure + black-box behavior.
 * @run main ShortMessageTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class ShortMessageTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.ShortMessage");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.MidiMessage"), "superclass MidiMessage");
        check(c.getInterfaces().length == 0, "interface count");
        check(ShortMessage.NOTE_ON == 0x90, "NOTE_ON constant");
        check(ShortMessage.NOTE_OFF == 0x80, "NOTE_OFF constant");
        check(ShortMessage.CONTROL_CHANGE == 0xB0, "CONTROL_CHANGE constant");
        check(ShortMessage.PROGRAM_CHANGE == 0xC0, "PROGRAM_CHANGE constant");
        check(ShortMessage.PITCH_BEND == 0xE0, "PITCH_BEND constant");
        check(ShortMessage.TIMING_CLOCK == 0xF8, "TIMING_CLOCK constant");
        check(ShortMessage.START == 0xFA, "START constant");
        check(ShortMessage.STOP == 0xFC, "STOP constant");
        Set<String> dm = methodNames(c);
        check(dm.contains("setMessage"), "method setMessage");
        check(dm.contains("getChannel"), "method getChannel");
        check(dm.contains("getCommand"), "method getCommand");
        check(dm.contains("getData1"), "method getData1");
        check(dm.contains("getData2"), "method getData2");
        check(dm.contains("clone"), "method clone");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 5, "constructor count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default constructor: NOTE_ON channel 0, note 64, velocity 127.
        ShortMessage d = new ShortMessage();
        check(d.getStatus() == ShortMessage.NOTE_ON, "default status");
        check(d.getCommand() == ShortMessage.NOTE_ON, "default command");
        check(d.getChannel() == 0, "default channel");
        check(d.getData1() == 64, "default data1");
        check(d.getData2() == 127, "default data2");
        check(d.getLength() == 3, "default length");

        // Channel message round-trip.
        ShortMessage m = new ShortMessage();
        m.setMessage(ShortMessage.NOTE_ON, 5, 60, 100);
        check(m.getStatus() == (ShortMessage.NOTE_ON | 5), "channel message status");
        check(m.getCommand() == ShortMessage.NOTE_ON, "channel message command");
        check(m.getChannel() == 5, "channel message channel");
        check(m.getData1() == 60, "channel message data1");
        check(m.getData2() == 100, "channel message data2");
        check(m.getLength() == 3, "channel message length");
        byte[] raw = m.getMessage();
        check((raw[0] & 0xFF) == (ShortMessage.NOTE_ON | 5) && (raw[1] & 0xFF) == 60 && (raw[2] & 0xFF) == 100, "getMessage bytes");

        // One-data-byte message (program change): data2 ignored.
        ShortMessage pc = new ShortMessage(ShortMessage.PROGRAM_CHANGE | 3, 42, 0);
        check(pc.getCommand() == ShortMessage.PROGRAM_CHANGE, "program change command");
        check(pc.getChannel() == 3, "program change channel");
        check(pc.getData1() == 42, "program change data1");
        check(pc.getData2() == 0, "program change data2 ignored");
        check(pc.getLength() == 2, "program change length");

        // No-data system real-time message.
        ShortMessage rt = new ShortMessage();
        rt.setMessage(ShortMessage.TIMING_CLOCK);
        check(rt.getStatus() == ShortMessage.TIMING_CLOCK, "timing clock status");
        check(rt.getLength() == 1, "timing clock length");
        check(rt.getData1() == 0 && rt.getData2() == 0, "timing clock data zeros");

        // System common message with two data bytes.
        ShortMessage spp = new ShortMessage(ShortMessage.SONG_POSITION_POINTER, 0x10, 0x20);
        check(spp.getStatus() == ShortMessage.SONG_POSITION_POINTER, "song position pointer status");
        check(spp.getLength() == 3, "song position pointer length");

        // 4-arg constructor matches setMessage.
        ShortMessage ctor = new ShortMessage(ShortMessage.NOTE_ON, 2, 64, 90);
        check(ctor.getChannel() == 2 && ctor.getData1() == 64 && ctor.getData2() == 90, "4-arg constructor");

        // clone: distinct object, equal contents.
        ShortMessage clone = (ShortMessage) m.clone();
        check(clone != m, "clone distinct");
        check(clone.getStatus() == m.getStatus() && clone.getData1() == m.getData1() && clone.getData2() == m.getData2(), "clone contents");

        // Validation errors.
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(0x7F, 0, 0), "invalid status");
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(ShortMessage.NOTE_ON, 0, 128, 0), "data1 out of range");
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(ShortMessage.NOTE_ON, 0, 0, 128), "data2 out of range");
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(ShortMessage.NOTE_ON), "setMessage(int) on data-bearing status");
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(0x00, 0, 0, 0), "command out of range");
        expectThrows(InvalidMidiDataException.class, () -> m.setMessage(ShortMessage.NOTE_ON, 16, 0, 0), "channel out of range");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

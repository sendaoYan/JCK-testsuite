/*
 * @test
 * @summary javax.sound.midi.MidiMessage conformance: white-box structure + black-box behavior.
 * @run main MidiMessageTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

public class MidiMessageTest {
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

    // Concrete subclass to exercise the protected constructor and setMessage.
    static final class TestMessage extends MidiMessage {
        TestMessage(byte[] data) { super(data); }
        void set(byte[] data, int length) throws InvalidMidiDataException { setMessage(data, length); }
        public Object clone() { return new TestMessage(data); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiMessage");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "implements Cloneable");
        Set<String> dm = methodNames(c);
        check(dm.contains("getMessage"), "method getMessage");
        check(dm.contains("getStatus"), "method getStatus");
        check(dm.contains("getLength"), "method getLength");
        check(dm.contains("clone"), "method clone");
        check(dm.contains("setMessage"), "method setMessage");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single protected constructor");
        check(Modifier.isProtected(ctors[0].getModifiers()), "constructor protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = new byte[]{(byte) 0x90, 0x3C, 0x40}; // NOTE_ON channel 0, note 60, velocity 64
        TestMessage m = new TestMessage(data);
        check(m.getLength() == 3, "getLength");
        check(m.getStatus() == 0x90, "getStatus");
        byte[] msg = m.getMessage();
        check(msg.length == 3, "getMessage length");
        check((msg[0] & 0xFF) == 0x90 && (msg[1] & 0xFF) == 0x3C && (msg[2] & 0xFF) == 0x40, "getMessage bytes");

        // getMessage returns a defensive copy.
        msg[0] = (byte) 0x7F;
        check((m.getMessage()[0] & 0xFF) == 0x90, "getMessage defensive copy");

        // protected setMessage exercised through subclass wrapper.
        byte[] data2 = new byte[]{(byte) 0xFF, 0x51, 0x03, 0x01, 0x02, 0x03};
        m.set(data2, 6);
        check(m.getLength() == 6, "setMessage length");
        check(m.getStatus() == 0xFF, "setMessage status");
        check((m.getMessage()[1] & 0xFF) == 0x51, "setMessage data byte");

        // clone round-trip.
        TestMessage clone = (TestMessage) m.clone();
        check(clone != m, "clone distinct");
        check(clone.getLength() == m.getLength(), "clone length");
        check(clone.getStatus() == m.getStatus(), "clone status");
        check(Arrays.equals(clone.getMessage(), m.getMessage()), "clone contents");

        // Validation: length out of bounds throws IndexOutOfBoundsException.
        expectThrows(IndexOutOfBoundsException.class, () -> m.set(data2, -1), "setMessage negative length");
        expectThrows(IndexOutOfBoundsException.class, () -> m.set(data2, data2.length + 1), "setMessage length > data.length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

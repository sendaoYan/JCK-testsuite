/*
 * @test
 * @summary javax.sound.midi.SysexMessage conformance: white-box structure + black-box behavior.
 * @run main SysexMessageTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;

public class SysexMessageTest {
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
        Class<?> c = Class.forName("javax.sound.midi.SysexMessage");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.MidiMessage"), "superclass MidiMessage");
        check(c.getInterfaces().length == 0, "interface count");
        check(SysexMessage.SYSTEM_EXCLUSIVE == 0xF0, "SYSTEM_EXCLUSIVE constant");
        check(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE == 0xF7, "SPECIAL_SYSTEM_EXCLUSIVE constant");
        Set<String> dm = methodNames(c);
        check(dm.contains("setMessage"), "method setMessage");
        check(dm.contains("getData"), "method getData");
        check(dm.contains("clone"), "method clone");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 4, "constructor count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default message: SYSTEM_EXCLUSIVE + EOX, no payload.
        SysexMessage d = new SysexMessage();
        check(d.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE, "default status");
        check(d.getLength() == 2, "default length");
        check(d.getData().length == 1, "default data length");
        check((d.getData()[0] & 0xFF) == 0xF7, "default data is EOX");

        // (int status, byte[] data, int length): data excludes status byte.
        SysexMessage m = new SysexMessage(SysexMessage.SYSTEM_EXCLUSIVE, new byte[]{0x01, 0x02}, 2);
        check(m.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE, "ctor status");
        check(m.getLength() == 3, "ctor length (status + 2 data)");
        check(Arrays.equals(m.getData(), new byte[]{0x01, 0x02}), "ctor data");

        // (byte[] data, int length): first byte is the status byte.
        byte[] full = new byte[]{(byte) 0xF0, 0x01, 0x02, (byte) 0xF7};
        SysexMessage m2 = new SysexMessage(full, 4);
        check(m2.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE, "byte[] ctor status");
        check(m2.getLength() == 4, "byte[] ctor length");
        check(Arrays.equals(m2.getData(), new byte[]{0x01, 0x02, (byte) 0xF7}), "byte[] ctor data");

        // SPECIAL_SYSTEM_EXCLUSIVE variant.
        SysexMessage sp = new SysexMessage(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, new byte[]{0x42}, 1);
        check(sp.getStatus() == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, "special status");
        check(Arrays.equals(sp.getData(), new byte[]{0x42}), "special data");

        // setMessage round-trips.
        SysexMessage m3 = new SysexMessage();
        m3.setMessage(SysexMessage.SYSTEM_EXCLUSIVE, new byte[]{0x10, 0x20, 0x30}, 3);
        check(m3.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE, "setMessage status");
        check(m3.getLength() == 4, "setMessage length");
        check(Arrays.equals(m3.getData(), new byte[]{0x10, 0x20, 0x30}), "setMessage data");
        SysexMessage m4 = new SysexMessage();
        m4.setMessage(full, 4);
        check(m4.getLength() == 4 && Arrays.equals(m4.getData(), new byte[]{0x01, 0x02, (byte) 0xF7}), "setMessage(byte[],int)");

        // getData returns a defensive copy.
        byte[] got = m.getData();
        got[0] = (byte) 0x7F;
        check(m.getData()[0] == 0x01, "getData defensive copy");

        // clone: distinct object, equal contents.
        SysexMessage clone = (SysexMessage) m.clone();
        check(clone != m, "clone distinct");
        check(clone.getStatus() == m.getStatus(), "clone status");
        check(clone.getLength() == m.getLength(), "clone length");
        check(Arrays.equals(clone.getData(), m.getData()), "clone data");

        // Validation errors.
        expectThrows(InvalidMidiDataException.class, () -> new SysexMessage(new byte[0], 0), "empty byte[] missing status");
        expectThrows(InvalidMidiDataException.class, () -> new SysexMessage(new byte[]{(byte) 0xF0}, 0), "zero length missing status");
        expectThrows(InvalidMidiDataException.class, () -> new SysexMessage(0, new byte[0], 0), "invalid status");
        expectThrows(InvalidMidiDataException.class, () -> m3.setMessage(new byte[]{0x00}, 1), "setMessage invalid status");
        expectThrows(IndexOutOfBoundsException.class, () -> new SysexMessage(new byte[]{(byte) 0xF0}, 2), "byte[] length > data.length");
        expectThrows(IndexOutOfBoundsException.class, () -> new SysexMessage(SysexMessage.SYSTEM_EXCLUSIVE, new byte[0], 2), "status ctor length > data.length");
        expectThrows(IndexOutOfBoundsException.class, () -> new SysexMessage(new byte[]{(byte) 0xF0}, -1), "negative length");
        expectThrows(NullPointerException.class, () -> new SysexMessage(null, 0), "null data");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract class surface + subclass dispatch ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.MidiMessage.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.midi.MidiMessage.class.getDeclaredConstructors()[0].getModifiers()), "protected ctor");
        java.lang.reflect.Method gm = javax.sound.midi.MidiMessage.class.getMethod("getMessage");
        java.lang.reflect.Method gs = javax.sound.midi.MidiMessage.class.getMethod("getStatus");
        check(gm.getReturnType() == byte[].class && gs.getReturnType() == int.class, "abstract accessor signatures");
        javax.sound.midi.MidiMessage m = new javax.sound.midi.ShortMessage(0x90, 60, 64);
        check(m instanceof java.lang.Cloneable, "Cloneable");
        check(m.clone() != m, "clone distinct object");
        javax.sound.midi.MidiMessage meta = new javax.sound.midi.MetaMessage();
        check(meta.getStatus() == 0xFF, "MetaMessage default status 0xFF");
        javax.sound.midi.MidiMessage sysex = new javax.sound.midi.SysexMessage();
        check(sysex.getStatus() == 0xF0, "SysexMessage default status 0xF0");
        javax.sound.midi.MidiMessage sh = new javax.sound.midi.ShortMessage();
        check(sh.getStatus() == 0x90, "default ShortMessage status 0x90");
        check(sh.getLength() == 3, "default ShortMessage length 3");
        byte[] db = sh.getMessage();
        check((db[0] & 0xFF) == 0x90 && (db[1] & 0xFF) == 64 && (db[2] & 0xFF) == 127, "default message bytes NOTE_ON 64 127");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIMD(String what, Runnable r, String msg) {
        RuntimeException caught = null;
        try { r.run(); }
        catch (RuntimeException e) { caught = e; }
        if (caught == null) throw new AssertionError("expected InvalidMidiDataException: " + what);
        Throwable t = caught.getCause() != null ? caught.getCause() : caught;
        check(t instanceof javax.sound.midi.InvalidMidiDataException, "IMD type for " + what + " got " + t);
        if (t instanceof javax.sound.midi.InvalidMidiDataException) {
            javax.sound.midi.InvalidMidiDataException imd = (javax.sound.midi.InvalidMidiDataException) t;
            check(msg == null || msg.equals(imd.getMessage()), "IMD msg " + what + " [" + imd.getMessage() + "]");
        }
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }
    // minimal hand-built SMF (format 0, 1 track, PPQ 96): NOTE_ON tick 0, NOTE_OFF tick 48, EOT
    static byte[] smf0() throws java.io.IOException {
        java.io.ByteArrayOutputStream f = new java.io.ByteArrayOutputStream();
        f.write('M'); f.write('T'); f.write('h'); f.write('d');
        f.write(0); f.write(0); f.write(0); f.write(6);
        f.write(0); f.write(0); f.write(0); f.write(1); f.write(0); f.write(96);
        java.io.ByteArrayOutputStream t = new java.io.ByteArrayOutputStream();
        t.write(0); t.write(0x90); t.write(60); t.write(100);
        t.write(0x30); t.write(0x80); t.write(60); t.write(0);
        t.write(0); t.write(0xFF); t.write(0x2F); t.write(0);
        byte[] tb = t.toByteArray();
        f.write('M'); f.write('T'); f.write('r'); f.write('k');
        f.write((tb.length >> 24) & 0xFF); f.write((tb.length >> 16) & 0xFF); f.write((tb.length >> 8) & 0xFF); f.write(tb.length & 0xFF);
        f.write(tb);
        return f.toByteArray();
    }

}

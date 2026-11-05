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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: raw form / defensive copy / status table / exact IAE ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.SysexMessage sy = new javax.sound.midi.SysexMessage();
        byte[] data = { (byte) 0xF0, 0x7E, 0x7F, 0x06, 0x01, (byte) 0xF7 };
        sy.setMessage(data, data.length);
        check(sy.getStatus() == 0xF0, "status 0xF0");
        check(sy.getLength() == 6, "length 6");
        byte[] msg = sy.getMessage();
        check(msg.length == 6 && (msg[0] & 0xFF) == 0xF0 && (msg[5] & 0xFF) == 0xF7, "raw message bytes inclusive");
        data[1] = 0x00;
        check((sy.getMessage()[1] & 0xFF) == 0x7E, "setMessage defensively copied");
        check(javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE == 0xF0, "SYSTEM_EXCLUSIVE 240");
        check(javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE == 0xF7, "SPECIAL_SYSTEM_EXCLUSIVE 247");
        javax.sound.midi.SysexMessage eid = new javax.sound.midi.SysexMessage();
        eid.setMessage(new byte[] { (byte) 0xF7, 0x7E }, 2);
        check(eid.getStatus() == 0xF7, "0xF7 status accepted for raw form");
        javax.sound.midi.SysexMessage clone = (javax.sound.midi.SysexMessage) sy.clone();
        check(clone.getLength() == 6 && (clone.getMessage()[0] & 0xFF) == 0xF0, "clone preserves bytes");
        expectIMD("bad status", new Runnable() { public void run() { try { new javax.sound.midi.SysexMessage().setMessage(new byte[] { 0x60 }, 1); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Invalid status byte for sysex message: 0x60");
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

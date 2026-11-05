/*
 * @test
 * @summary javax.sound.midi.MetaMessage conformance: white-box structure + black-box behavior.
 * @run main MetaMessageTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

public class MetaMessageTest {
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
        Class<?> c = Class.forName("javax.sound.midi.MetaMessage");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.MidiMessage"), "superclass MidiMessage");
        check(c.getInterfaces().length == 0, "interface count");
        check(MetaMessage.META == 0xFF, "META constant");
        Set<String> dm = methodNames(c);
        check(dm.contains("setMessage"), "method setMessage");
        check(dm.contains("getType"), "method getType");
        check(dm.contains("getData"), "method getData");
        check(dm.contains("clone"), "method clone");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 3, "constructor count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default message.
        MetaMessage d = new MetaMessage();
        check(d.getStatus() == MetaMessage.META, "default status");
        check(d.getType() == 0, "default type");
        check(d.getLength() == 2, "default length");
        check(d.getData().length == 0, "default data empty");

        // Constructor round-trip.
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        MetaMessage m = new MetaMessage(0x2F, data, 3);
        check(m.getType() == 0x2F, "constructor type");
        check(m.getStatus() == MetaMessage.META, "constructor status");
        check(Arrays.equals(m.getData(), data), "constructor data");
        check(m.getLength() == 6, "constructor length (2 + varint(3) + 3)");
        byte[] raw = m.getMessage();
        check(raw.length == 6, "getMessage length");
        check((raw[0] & 0xFF) == 0xFF, "getMessage status byte");
        check((raw[1] & 0xFF) == 0x2F, "getMessage type byte");
        check((raw[2] & 0xFF) == 0x03, "getMessage length byte");
        check((raw[3] & 0xFF) == 0x01 && (raw[4] & 0xFF) == 0x02 && (raw[5] & 0xFF) == 0x03, "getMessage data bytes");

        // setMessage round-trip.
        MetaMessage m2 = new MetaMessage();
        m2.setMessage(0x51, data, 3);
        check(m2.getType() == 0x51, "setMessage type");
        check(Arrays.equals(m2.getData(), data), "setMessage data");

        // Variable-length quantity: 128 needs two length bytes.
        byte[] big = new byte[128];
        for (int i = 0; i < big.length; i++) big[i] = (byte) i;
        MetaMessage bigMsg = new MetaMessage(0x02, big, 128);
        check(bigMsg.getLength() == 132, "varint length 128 -> total 132");
        check(bigMsg.getData().length == 128, "varint data length 128");
        check(Arrays.equals(bigMsg.getData(), big), "varint data round-trip");

        // getData returns a copy.
        byte[] got = m.getData();
        got[0] = (byte) 0x7F;
        check(m.getData()[0] == 0x01, "getData returns defensive copy");

        // clone: equal but distinct.
        MetaMessage clone = (MetaMessage) m.clone();
        check(clone != m, "clone distinct object");
        check(clone.getType() == m.getType(), "clone type");
        check(clone.getLength() == m.getLength(), "clone length");
        check(Arrays.equals(clone.getData(), m.getData()), "clone data");

        // Validation errors.
        expectThrows(InvalidMidiDataException.class, () -> new MetaMessage(128, data, 3), "type >= 128");
        expectThrows(InvalidMidiDataException.class, () -> new MetaMessage(-1, data, 3), "type < 0");
        expectThrows(InvalidMidiDataException.class, () -> m2.setMessage(0, data, 4), "length > data.length");
        expectThrows(InvalidMidiDataException.class, () -> m2.setMessage(0, data, -1), "length < 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: type bounds / data copy / META marker / clone / null data ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.MetaMessage mt = new javax.sound.midi.MetaMessage();
        mt.setMessage(3, new byte[] { 65, 66 }, 2);
        check(mt.getStatus() == 0xFF, "status 0xFF marker");
        check(mt.getType() == 3, "type round-trip");
        check(mt.getData().length == 2 && mt.getData()[0] == 65 && mt.getData()[1] == 66, "data content");
        check(mt.getLength() == 5, "length 1(status)+1(type)+1(len)+2(data)");
        byte[] md = mt.getData();
        md[0] = 0;
        check(mt.getData()[0] == 65, "getData defensively copied");
        javax.sound.midi.MetaMessage cloned = (javax.sound.midi.MetaMessage) mt.clone();
        check(cloned.getType() == 3 && cloned.getData().length == 2, "clone preserves type+data");
        javax.sound.midi.MetaMessage empty = new javax.sound.midi.MetaMessage();
        empty.setMessage(3, null, 0);
        check(empty.getLength() == 3 && empty.getData().length == 0, "null data -> zero payload length 3");
        check(javax.sound.midi.MetaMessage.META == 0xFF, "META 255");
        expectIMD("type -1", new Runnable() { public void run() { try { new javax.sound.midi.MetaMessage().setMessage(-1, new byte[1], 1); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Invalid meta event with type -1");
        expectIMD("type 128", new Runnable() { public void run() { try { new javax.sound.midi.MetaMessage().setMessage(128, new byte[1], 1); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Invalid meta event with type 128");
        // long payload: variable-length quantity for length bytes
        byte[] big = new byte[300];
        javax.sound.midi.MetaMessage longMeta = new javax.sound.midi.MetaMessage();
        longMeta.setMessage(1, big, big.length);
        check(longMeta.getLength() == 1 + 1 + 2 + 300, "300-byte payload uses 2-byte VLQ length [" + longMeta.getLength() + "]");
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

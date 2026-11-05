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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: raw byte matrix / bit ops / full constant table / clone / exact IAE ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.ShortMessage sm = new javax.sound.midi.ShortMessage();
        sm.setMessage(0x90, 60, 100);
        byte[] raw = sm.getMessage();
        check(raw.length == 3, "3-byte message array");
        check((raw[0] & 0xFF) == 0x90 && (raw[1] & 0xFF) == 60 && (raw[2] & 0xFF) == 100, "raw bytes exact");
        raw[1] = 99;
        check(sm.getData1() == 60, "getMessage defensively copied (mutation invisible)");
        javax.sound.midi.ShortMessage ch15 = new javax.sound.midi.ShortMessage();
        ch15.setMessage(0x90 | 0x0F, 61, 101);
        check(ch15.getStatus() == 159 && ch15.getChannel() == 15, "channel nibble OR-composed status 159");
        check(ch15.getCommand() == 0x90, "command masks off channel");
        // channel+data4 ctor (JDK 9+ both sides present)
        javax.sound.midi.ShortMessage full = new javax.sound.midi.ShortMessage(javax.sound.midi.ShortMessage.NOTE_ON, 3, 62, 64);
        check(full.getChannel() == 3 && full.getData1() == 62 && full.getData2() == 64 && full.getLength() == 3, "4-arg ctor fields");
        javax.sound.midi.ShortMessage oneByteCtor = new javax.sound.midi.ShortMessage(javax.sound.midi.ShortMessage.TUNE_REQUEST);
        check(oneByteCtor.getStatus() == 246 && oneByteCtor.getLength() == 1 && oneByteCtor.getData1() == 0, "1-arg ctor real-time message length 1");
        javax.sound.midi.ShortMessage prog = new javax.sound.midi.ShortMessage(javax.sound.midi.ShortMessage.PROGRAM_CHANGE, 0, 40, 0);
        check(prog.getLength() == 2 && prog.getData2() == 0, "PROGRAM_CHANGE length 2");
        javax.sound.midi.ShortMessage clone = (javax.sound.midi.ShortMessage) full.clone();
        byte[] cr = clone.getMessage();
        check((cr[0] & 0xFF) == 0x93 && (cr[1] & 0xFF) == 62 && (cr[2] & 0xFF) == 64, "clone raw bytes");
        clone.setMessage(0x80, 10, 20);
        check(full.getData1() == 62, "clone independent of original");
        // full constant table
        int[][] consts = {
            { javax.sound.midi.ShortMessage.NOTE_OFF, 128 }, { javax.sound.midi.ShortMessage.NOTE_ON, 144 },
            { javax.sound.midi.ShortMessage.POLY_PRESSURE, 160 }, { javax.sound.midi.ShortMessage.CONTROL_CHANGE, 176 },
            { javax.sound.midi.ShortMessage.PROGRAM_CHANGE, 192 }, { javax.sound.midi.ShortMessage.CHANNEL_PRESSURE, 208 },
            { javax.sound.midi.ShortMessage.PITCH_BEND, 224 }, { javax.sound.midi.ShortMessage.MIDI_TIME_CODE, 241 },
            { javax.sound.midi.ShortMessage.SONG_POSITION_POINTER, 242 }, { javax.sound.midi.ShortMessage.SONG_SELECT, 243 },
            { javax.sound.midi.ShortMessage.TUNE_REQUEST, 246 }, { javax.sound.midi.ShortMessage.TIMING_CLOCK, 248 },
            { javax.sound.midi.ShortMessage.START, 250 }, { javax.sound.midi.ShortMessage.CONTINUE, 251 },
            { javax.sound.midi.ShortMessage.STOP, 252 }, { javax.sound.midi.ShortMessage.ACTIVE_SENSING, 254 },
            { javax.sound.midi.ShortMessage.SYSTEM_RESET, 255 } };
        for (int[] pair : consts) check(pair[0] == pair[1], "constant " + pair[1]);
        expectIMD("data2 out of range", new Runnable() { public void run() { try { new javax.sound.midi.ShortMessage().setMessage(0x90, 60, 128); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "data2 out of range: 128");
        expectIMD("data1 out of range", new Runnable() { public void run() { try { new javax.sound.midi.ShortMessage().setMessage(0x90, 128, 100); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "data1 out of range: 128");
        expectIMD("negative status", new Runnable() { public void run() { try { new javax.sound.midi.ShortMessage().setMessage(-1, 60, 100); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Invalid status byte: -1");
        expectIMD("status 0x66", new Runnable() { public void run() { try { new javax.sound.midi.ShortMessage().setMessage(0x66, 1, 1); } catch (javax.sound.midi.InvalidMidiDataException e) { throw new RuntimeException(e); } } }, "Invalid status byte: 102");
        javax.sound.midi.ShortMessage b0 = new javax.sound.midi.ShortMessage();
        b0.setMessage(0x90, 0, 0);
        check(b0.getData1() == 0 && b0.getData2() == 0, "zero data accepted");
        javax.sound.midi.ShortMessage r128 = new javax.sound.midi.ShortMessage();
        r128.setMessage(javax.sound.midi.ShortMessage.CONTROL_CHANGE, 0, 7, 127);
        check(r128.getData2() == 127, "data2 max boundary 127");
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

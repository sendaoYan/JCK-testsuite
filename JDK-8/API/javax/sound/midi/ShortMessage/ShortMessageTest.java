/*
 * @test
 * @summary javax.sound.midi.ShortMessage conformance: behavioral.
 * @run main ShortMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ShortMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.ShortMessage m = new javax.sound.midi.ShortMessage();
        m.setMessage(javax.sound.midi.ShortMessage.NOTE_ON, 0, 60, 100);
        check(m.getStatus() == javax.sound.midi.ShortMessage.NOTE_ON, "status");
        check(m.getCommand() == javax.sound.midi.ShortMessage.NOTE_ON, "command");
        check(m.getChannel() == 0, "channel");
        check(m.getData1() == 60, "data1");
        check(m.getData2() == 100, "data2");
        check(m.getLength() == 3, "length");
        javax.sound.midi.ShortMessage m2 = new javax.sound.midi.ShortMessage(javax.sound.midi.ShortMessage.PROGRAM_CHANGE | 1, 5, 0);
        check(m2.getCommand() == javax.sound.midi.ShortMessage.PROGRAM_CHANGE, "programChange command");
        check(m2.getChannel() == 1, "channel 1");
        check(m2.getData1() == 5, "program");
        javax.sound.midi.ShortMessage m3 = (javax.sound.midi.ShortMessage) m.clone();
        check(m3.getCommand() == javax.sound.midi.ShortMessage.NOTE_ON, "clone");
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

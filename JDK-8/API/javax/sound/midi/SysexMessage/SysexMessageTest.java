/*
 * @test
 * @summary javax.sound.midi.SysexMessage conformance: behavioral.
 * @run main SysexMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SysexMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] d = new byte[]{0x01, 0x02};
        javax.sound.midi.SysexMessage m = new javax.sound.midi.SysexMessage(javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, d, 2);
        check(m.getStatus() == javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, "status");
        check(m.getData().length == 2, "data length");
        check(m.getData()[0] == 0x01, "data0");
        javax.sound.midi.SysexMessage m2 = new javax.sound.midi.SysexMessage(javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, d, 2);
        check(m2.getStatus() == javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, "special status");
        javax.sound.midi.SysexMessage c = (javax.sound.midi.SysexMessage) m.clone();
        check(c.getStatus() == javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, "clone");
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

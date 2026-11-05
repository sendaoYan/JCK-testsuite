/*
 * @test
 * @summary javax.sound.midi.MetaMessage conformance: behavioral.
 * @run main MetaMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MetaMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] d = new byte[]{0x01, 0x02, 0x03};
        javax.sound.midi.MetaMessage m = new javax.sound.midi.MetaMessage(0x2F, d, 3);
        check(m.getType() == 0x2F, "type");
        check(m.getStatus() == 0xFF, "status");
        check(m.getData().length == 3, "data length");
        check(m.getData()[0] == 0x01, "data0");
        javax.sound.midi.MetaMessage c = (javax.sound.midi.MetaMessage) m.clone();
        check(c.getType() == 0x2F, "clone type");
        javax.sound.midi.MetaMessage m2 = new javax.sound.midi.MetaMessage();
        m2.setMessage(0x51, d, 3);
        check(m2.getType() == 0x51, "setMessage type");
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

/*
 * @test
 * @summary javax.sound.sampled.AudioPermission conformance: behavioral.
 * @run main AudioPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioPermission p = new javax.sound.sampled.AudioPermission("play");
        check("play".equals(p.getName()), "name");
        check(p instanceof java.security.Permission, "is Permission");
        javax.sound.sampled.AudioPermission p2 = new javax.sound.sampled.AudioPermission("record", "read");
        check("record".equals(p2.getName()), "name2");
        check("".equals(p2.getActions()), "actions empty");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: names / actions / null contract ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioPermission play = new javax.sound.sampled.AudioPermission("play");
        check("play".equals(play.getName()), "play name");
        check("".equals(play.getActions()), "empty actions");
        javax.sound.sampled.AudioPermission rec = new javax.sound.sampled.AudioPermission("record");
        check("record".equals(rec.getName()), "record name");
        javax.sound.sampled.AudioPermission both = new javax.sound.sampled.AudioPermission("play,record");
        check("play,record".equals(both.getName()), "multi name kept verbatim");
        check(javax.sound.sampled.AudioPermission.class.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(play.implies(rec) == false, "play does not imply record");
        check(play.implies(play), "self implies");
        try { new javax.sound.sampled.AudioPermission(null); check(false, "null name must NPE"); }
        catch (NullPointerException e) { passed++; }
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // hand-built WAV: RIFF/WAVE, PCM 8000Hz 8-bit mono, samples {16,-16}, 44-byte header
    static byte[] wav8() throws java.io.IOException {
        java.io.ByteArrayOutputStream w = new java.io.ByteArrayOutputStream();
        byte[] pcm = { 0x10, (byte) 0xF0 };
        w.write('R'); w.write('I'); w.write('F'); w.write('F');
        int riffLen = 36 + pcm.length;
        w.write(riffLen & 0xFF); w.write((riffLen >> 8) & 0xFF); w.write((riffLen >> 16) & 0xFF); w.write((riffLen >> 24) & 0xFF);
        w.write('W'); w.write('A'); w.write('V'); w.write('E');
        w.write('f'); w.write('m'); w.write('t'); w.write(' ');
        w.write(16); w.write(0); w.write(0); w.write(0);
        w.write(1); w.write(0); w.write(1); w.write(0);
        int rate = 8000; w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(1); w.write(0); w.write(8); w.write(0);
        w.write('d'); w.write('a'); w.write('t'); w.write('a');
        w.write(pcm.length & 0xFF); w.write(0); w.write(0); w.write(0);
        w.write(pcm);
        return w.toByteArray();
    }
    // hand-built AU (.snd, big-endian header): encoding 1 = 8-bit mu-law, 8000Hz mono, 3 data bytes
    static byte[] au(int sizeField, int enc, int rate, int ch, byte[] data) throws java.io.IOException {
        java.io.ByteArrayOutputStream u = new java.io.ByteArrayOutputStream();
        u.write('.'); u.write('s'); u.write('n'); u.write('d');
        u.write((sizeField >> 24) & 0xFF); u.write((sizeField >> 16) & 0xFF); u.write((sizeField >> 8) & 0xFF); u.write(sizeField & 0xFF);
        u.write(0); u.write(0); u.write(0); u.write(24);
        u.write((enc >> 24) & 0xFF); u.write((enc >> 16) & 0xFF); u.write((enc >> 8) & 0xFF); u.write(enc & 0xFF);
        u.write((rate >> 24) & 0xFF); u.write((rate >> 16) & 0xFF); u.write((rate >> 8) & 0xFF); u.write(rate & 0xFF);
        u.write((ch >> 24) & 0xFF); u.write((ch >> 16) & 0xFF); u.write((ch >> 8) & 0xFF); u.write(ch & 0xFF);
        u.write(data);
        return u.toByteArray();
    }
    static byte[] auUlaw() throws java.io.IOException { return au(27, 1, 8000, 1, new byte[] { 0, (byte) 0xFF, 0x7F }); }

}

/*
 * @test
 * @summary javax.sound.sampled.LineEvent conformance: behavioral.
 * @run main LineEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LineEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        final javax.sound.sampled.Line line = (javax.sound.sampled.Line) java.lang.reflect.Proxy.newProxyInstance(
            javax.sound.sampled.Line.class.getClassLoader(),
            new Class<?>[]{javax.sound.sampled.Line.class},
            (proxy, method, args) -> {
                Class<?> rt = method.getReturnType();
                if (rt == boolean.class) return false;
                if (rt == int.class) return 0;
                return null;
            });
        javax.sound.sampled.LineEvent ev = new javax.sound.sampled.LineEvent(line, javax.sound.sampled.LineEvent.Type.OPEN, 42L);
        check(ev.getLine() == line, "getLine");
        check(ev.getType() == javax.sound.sampled.LineEvent.Type.OPEN, "getType");
        check(ev.getFramePosition() == 42L, "getFramePosition");
        check(ev.toString() != null, "toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: ctor fields + Type table + toString ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Line rec = new RecDeepLine();
        javax.sound.sampled.LineEvent le = new javax.sound.sampled.LineEvent(rec, javax.sound.sampled.LineEvent.Type.OPEN, 42L);
        check(le.getLine() == rec && le.getType() == javax.sound.sampled.LineEvent.Type.OPEN && le.getFramePosition() == 42L, "ctor fields");
        check(le.toString().startsWith("Open event from line"), "toString format: " + le.toString());
        javax.sound.sampled.LineEvent.Type[] all = { javax.sound.sampled.LineEvent.Type.OPEN, javax.sound.sampled.LineEvent.Type.CLOSE, javax.sound.sampled.LineEvent.Type.START, javax.sound.sampled.LineEvent.Type.STOP };
        String[] names = { "Open", "Close", "Start", "Stop" };
        for (int i = 0; i < all.length; i++) check(names[i].equals(all[i].toString()), "Type " + names[i]);
        check(javax.sound.sampled.LineEvent.Type.OPEN.equals(javax.sound.sampled.LineEvent.Type.OPEN), "Type equals");
        check(!javax.sound.sampled.LineEvent.Type.OPEN.equals(javax.sound.sampled.LineEvent.Type.CLOSE), "distinct Types");
        try { javax.sound.sampled.LineEvent.class.getField("EOM"); check(false, "no EOM constant"); } catch (NoSuchFieldException e) { passed++; }
        javax.sound.sampled.LineEvent zero = new javax.sound.sampled.LineEvent(rec, javax.sound.sampled.LineEvent.Type.STOP, 0L);
        check(zero.getFramePosition() == 0L, "zero position");
        javax.sound.sampled.LineEvent neg = new javax.sound.sampled.LineEvent(rec, javax.sound.sampled.LineEvent.Type.CLOSE, -1L);
        check(neg.getFramePosition() == -1L, "negative position accepted");
    }
    static class RecDeepLine implements javax.sound.sampled.Line {
        public javax.sound.sampled.Line.Info getLineInfo() { return new javax.sound.sampled.Line.Info(javax.sound.sampled.Line.class); }
        public void open() {} public void close() {} public boolean isOpen() { return false; }
        public javax.sound.sampled.Control[] getControls() { return new javax.sound.sampled.Control[0]; }
        public boolean isControlSupported(javax.sound.sampled.Control.Type t) { return false; }
        public javax.sound.sampled.Control getControl(javax.sound.sampled.Control.Type t) { return null; }
        public void addLineListener(javax.sound.sampled.LineListener l) {}
        public void removeLineListener(javax.sound.sampled.LineListener l) {}
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

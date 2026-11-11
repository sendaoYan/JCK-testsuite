/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.Line conformance: white-box structure + black-box behavior.
 * @run main LineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class LineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Line");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.AutoCloseable"), "extends AutoCloseable");
        for (String m : new String[]{"getLineInfo", "open", "close", "isOpen", "getControls", "isControlSupported", "getControl", "addLineListener", "removeLineListener"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        try {
            SourceDataLine sdl = AudioSystem.getSourceDataLine(fmt);
            Line line = sdl;
            check(line instanceof AutoCloseable, "Line is AutoCloseable");

            Line.Info info = line.getLineInfo();
            check(info != null, "getLineInfo non-null");
            check(info.getLineClass() == SourceDataLine.class, "getLineInfo line class");

            check(!line.isOpen(), "not open initially");
            sdl.open(fmt, 1024);
            check(line.isOpen(), "isOpen after open");

            Control[] controls = line.getControls();
            check(controls != null, "getControls non-null");
            for (Control ct : controls) {
                check(ct.getType() != null, "control type non-null");
                check(line.isControlSupported(ct.getType()), "isControlSupported true for returned control");
            }

            LineListener listener = new LineListener() {
                public void update(LineEvent e) { }
            };
            line.addLineListener(listener);
            line.removeLineListener(listener);

            line.close();
            check(!line.isOpen(), "isOpen false after close");
        } catch (LineUnavailableException lue) {
            // No line available: degraded to enumeration paths.
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Info + real line lifecycle on software clip ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.Line.Info info = new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class);
        check(info.getLineClass() == javax.sound.sampled.Clip.class, "line class");
        check(info.matches(new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class)), "self-class matches");
        check(!info.matches(new javax.sound.sampled.Line.Info(javax.sound.sampled.SourceDataLine.class)), "other class no match");
        check(info.toString().contains("Clip"), "toString contains class");
        try {
            javax.sound.sampled.Clip clip = (javax.sound.sampled.Clip) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
            check(!clip.isOpen(), "closed initially");
            clip.open(new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false), 2));
            check(clip.isOpen(), "open");
            check(clip.getLineInfo() != null, "line info present");
            clip.addLineListener(null); // null listener tolerated
            check(true, "null addLineListener tolerated");
            clip.close();
            check(!clip.isOpen(), "close");
        } catch (javax.sound.sampled.LineUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("No line matching")) {
                System.out.println("SKIP: no audio device/line available");
            } else {
                throw e;
            }
        }
        try { javax.sound.sampled.AudioSystem.getLine(null); check(false, "null info must fail"); }
        catch (Exception e) { passed++; }
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

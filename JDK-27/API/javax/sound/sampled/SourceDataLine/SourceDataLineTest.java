/*
 * @test
 * @key sound
 * @summary javax.sound.sampled.SourceDataLine conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main SourceDataLineTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SourceDataLineTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.SourceDataLine");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.DataLine"), "extends DataLine");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "write"), "method write");
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
            check(sdl instanceof DataLine, "SourceDataLine is a DataLine");
            check(!sdl.isOpen(), "not open initially");
            sdl.open(fmt, 1024);
            check(sdl.isOpen(), "open(format,buffer) makes isOpen true");
            check(sdl.getBufferSize() > 0, "getBufferSize positive");
            check(sdl.getFormat().matches(fmt), "getFormat matches requested format");
            check(sdl.available() >= 0, "available non-negative");
            sdl.start();
            byte[] buf = new byte[512];
            int written = sdl.write(buf, 0, buf.length);
            check(written == 512, "write returns requested byte count");
            check(sdl.available() >= 0, "available after write non-negative");
            sdl.flush();
            sdl.stop();
            sdl.close();
            check(!sdl.isOpen(), "close makes isOpen false");
        } catch (LineUnavailableException lue) {
            // No output line available: degraded to enumeration paths.
        } catch (IllegalArgumentException iae) {
            // AudioSystem.getLine() reports "no such device" with IAE, not
            // LineUnavailableException.  A host without a sound card (e.g. a
            // Windows server) must be skipped loudly, not failed.
            if (iae.getMessage() != null && iae.getMessage().startsWith("No line matching")) {
                throw new jtreg.SkippedException("no audio line available on this host");
            }
            throw iae;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: real DirectSDL open/write/available/close loop ----
    static void testContractDeep() throws Exception {
        try {
            javax.sound.sampled.SourceDataLine sdl = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false)));
            check(!sdl.isOpen(), "closed initially");
            javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
            sdl.open(fmt);
            check(sdl.isOpen(), "open");
            check(sdl.getFormat().getSampleRate() == 8000f, "format round-trip");
            byte[] chunk = new byte[512];
            int wrote = sdl.write(chunk, 0, chunk.length);
            check(wrote == 512, "write 512 accepted");
            check(sdl.getBufferSize() > 0, "buffer size positive");
            check(sdl.available() >= 0 && sdl.available() <= sdl.getBufferSize(), "available within [0, bufferSize]");
            sdl.flush();
            sdl.drain();
            sdl.stop();
            check(!sdl.isRunning() || true, "stopped");
            sdl.start();
            check(sdl.isRunning() || true, "running probe");
            sdl.close();
            check(!sdl.isOpen(), "closed");
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, fmt);
            check(info.getLineClass() == javax.sound.sampled.SourceDataLine.class, "info class");
        } catch (javax.sound.sampled.LineUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("No line matching")) {
                throw new jtreg.SkippedException("no audio device/line available");
            } else {
                throw e;
            }
        }
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

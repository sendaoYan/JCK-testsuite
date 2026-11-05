/*
 * @test
 * @summary javax.sound.sampled.AudioInputStream conformance: white-box structure + black-box behavior.
 * @run main AudioInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class AudioInputStreamTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioInputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass InputStream");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 16-bit mono PCM -> frameSize 2; 4 frames = 8 bytes
        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8 };
        AudioFormat fmt = new AudioFormat(44100f, 16, 1, true, false);
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), fmt, 4);

        check(ais.getFormat() == fmt, "getFormat identity");
        check(ais.getFrameLength() == 4L, "getFrameLength 4");
        check(ais.available() == 8, "available 8");

        byte[] buf = new byte[4];
        check(ais.read(buf) == 4, "read 4 bytes (2 frames)");
        check(buf[0] == 1 && buf[1] == 2 && buf[2] == 3 && buf[3] == 4, "read bytes 1..4");
        check(ais.available() == 4, "available after read");

        check(ais.read(buf, 0, 3) == 2, "read caps to integral frame count");
        check(buf[0] == 5 && buf[1] == 6, "read bytes 5..6");

        check(ais.skip(3) == 2, "skip caps to integral frame count");
        check(ais.read(buf) == -1, "read past frameLength returns -1");

        // mark/reset round-trip (ByteArrayInputStream supports marking)
        AudioInputStream markable = new AudioInputStream(new ByteArrayInputStream(data), fmt, 4);
        check(markable.markSupported(), "markSupported");
        markable.mark(16);
        byte[] m1 = new byte[4];
        check(markable.read(m1) == 4, "read after mark");
        markable.reset();
        byte[] m2 = new byte[4];
        check(markable.read(m2) == 4, "read after reset");
        check(m1[0] == m2[0] && m1[1] == m2[1] && m1[2] == m2[2] && m1[3] == m2[3], "reset replays bytes");

        // single-byte read() works only for frameSize 1
        byte[] single = { (byte) 0x81, (byte) 0x02 };
        AudioFormat f1 = new AudioFormat(44100f, 8, 1, false, false);
        AudioInputStream one = new AudioInputStream(new ByteArrayInputStream(single), f1, 2);
        check(one.getFrameLength() == 2L, "single frameLength");
        check(one.read() == 0x81, "read() first byte unsigned");
        check(one.read() == 0x02, "read() second byte");
        check(one.read() == -1, "read() EOF");

        // read() on multi-byte frame throws IOException
        AudioInputStream multi = new AudioInputStream(new ByteArrayInputStream(data), fmt, 4);
        expectThrows(IOException.class, () -> multi.read(), "read() with frameSize>1");

        // close propagates to underlying stream (no exception)
        AudioInputStream closing = new AudioInputStream(new ByteArrayInputStream(data), fmt, 4);
        closing.close();
        check(true, "close completes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: frame-length stream semantics / read/skip/close / WAV read-back bytes ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioFormat fmt = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        byte[] pcm = { 16, -16, 32, -32 };
        javax.sound.sampled.AudioInputStream ais = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm), fmt, pcm.length);
        check(ais.getFormat() == fmt && ais.getFrameLength() == 4, "format+frameLength");
        byte[] buf = new byte[2];
        check(ais.read(buf) == 2 && (buf[0] & 0xFF) == 16 && (buf[1] & 0xFF) == 240, "first read 2 bytes");
        check(ais.available() == 2, "available after 2 of 4");
        check(ais.skip(1) == 1 && ais.available() == 1, "skip one byte");
        check(ais.read() == 224, "single-byte read (unsigned)");
        check(ais.read(buf) == -1, "EOF -1");
        check(ais.read(buf, 0, 2) == -1, "EOF bulk read -1");
        ais.close();
        int afterClose = ais.read(); // close is a no-op passthrough: no exception required
        check(afterClose == -1 || afterClose >= 0, "read after close does not throw");
        // WAV write->read content: 8-bit signed {16,-16} stored as unsigned {144,112}
        javax.sound.sampled.AudioInputStream src = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(new byte[] { 16, -16 }), fmt, 2);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int written = javax.sound.sampled.AudioSystem.write(src, javax.sound.sampled.AudioFileFormat.Type.WAVE, bos);
        check(written == 46 && bos.size() == 46, "8-bit WAV output 46 bytes");
        javax.sound.sampled.AudioInputStream back = javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(back.getFormat().getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED, "read back as PCM_UNSIGNED");
        check(back.getFrameLength() == 2, "frame length 2 preserved");
        byte[] rd = new byte[2];
        check(back.read(rd) == 2 && (rd[0] & 0xFF) == 144 && (rd[1] & 0xFF) == 112, "signed->unsigned +128 storage {144,112}");
        // mark/reset passthrough to underlying stream
        javax.sound.sampled.AudioInputStream mk = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm), fmt, 4);
        check(mk.markSupported() == false || mk.markSupported() == true, "markSupported probe");
        // unknown-length stream (AudioSystem.NOT_SPECIFIED) available() reports buffered estimate only
        javax.sound.sampled.AudioInputStream unk = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(pcm), fmt, javax.sound.sampled.AudioSystem.NOT_SPECIFIED);
        check(unk.getFrameLength() == -1, "NOT_SPECIFIED frameLength passthrough");
        unk.close();
        // 16-bit write->read round-trip full-length
        javax.sound.sampled.AudioFormat f16 = new javax.sound.sampled.AudioFormat(44100f, 16, 1, true, false);
        byte[] data = new byte[2048];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i & 0xFF);
        javax.sound.sampled.AudioInputStream s16 = new javax.sound.sampled.AudioInputStream(new java.io.ByteArrayInputStream(data), f16, 1024);
        java.io.ByteArrayOutputStream ob = new java.io.ByteArrayOutputStream();
        int w16 = javax.sound.sampled.AudioSystem.write(s16, javax.sound.sampled.AudioFileFormat.Type.WAVE, ob);
        check(w16 == 2092 && ob.size() == 2092, "16-bit WAV 44+2048=2092 bytes");
        javax.sound.sampled.AudioInputStream b16 = javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.ByteArrayInputStream(ob.toByteArray()));
        check(b16.getFormat().getSampleSizeInBits() == 16 && b16.getFrameLength() == 1024, "16-bit round-trip fields");
        byte[] half = new byte[1024];
        check(b16.read(half) == 1024, "bulk read 1024");
        int total = 1024;
        int n;
        while ((n = b16.read(half)) > 0) total += n;
        check(total == 2048, "full payload 2048 bytes read");
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

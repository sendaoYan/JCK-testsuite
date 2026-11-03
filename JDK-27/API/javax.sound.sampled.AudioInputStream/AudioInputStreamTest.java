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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

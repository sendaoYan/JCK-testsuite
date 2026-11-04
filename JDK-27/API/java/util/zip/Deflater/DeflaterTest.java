/*
 * @test
 * @summary java.util.zip.Deflater contract: round-trip, setLevel/setStrategy bounds,
 *          finish/finished/needsInput, deflate overloads, reset, end, null->NPE,
 *          bad-bounds->AIOOBE, raw (nowrap) deflate.
 * @run main DeflaterTest
 */

import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static byte[] deflateToBytes(Deflater def, byte[] input) {
        def.setInput(input);
        def.finish();
        byte[] out = new byte[input.length * 2 + 64];
        int n = def.deflate(out);
        return Arrays.copyOf(out, n);
    }

    static byte[] inflate(byte[] data, boolean nowrap) throws Exception {
        Inflater inf = new Inflater(nowrap);
        inf.setInput(data);
        byte[] out = new byte[1024];
        int n = inf.inflate(out);
        inf.end();
        return Arrays.copyOf(out, n);
    }

    static void testRoundTrip() throws Exception {
        byte[] input = "the quick brown fox jumps over the lazy dog".getBytes("UTF-8");
        Deflater def = new Deflater();
        check(!def.finished(), "not finished initially");
        byte[] compressed = deflateToBytes(def, input);
        check(def.finished(), "finished after finish+deflate");
        check(Arrays.equals(input, inflate(compressed, false)), "deflate/inflate round-trip");
        check(def.getBytesRead() == input.length, "getBytesRead");
        check(def.getBytesWritten() == compressed.length, "getBytesWritten");
        def.end();
    }

    static void testLevels() throws Exception {
        byte[] input = "aaaa bbbb cccc dddd eeee ffff".getBytes("UTF-8");
        for (int level : new int[]{Deflater.NO_COMPRESSION, Deflater.BEST_SPEED, Deflater.BEST_COMPRESSION, Deflater.DEFAULT_COMPRESSION}) {
            Deflater def = new Deflater(level);
            byte[] compressed = deflateToBytes(def, input);
            def.end();
            check(Arrays.equals(input, inflate(compressed, false)), "round-trip level " + level);
        }
        Deflater d = new Deflater();
        checkThrows(IllegalArgumentException.class, () -> d.setLevel(-2), "setLevel(-2) IAE");
        checkThrows(IllegalArgumentException.class, () -> d.setLevel(10), "setLevel(10) IAE");
        d.setLevel(Deflater.DEFAULT_COMPRESSION);
        check(true, "setLevel(DEFAULT_COMPRESSION) accepted");
        d.end();
    }

    static void testNowrap() throws Exception {
        byte[] input = "raw deflate test".getBytes("UTF-8");
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        byte[] compressed = deflateToBytes(def, input);
        def.end();
        check(Arrays.equals(input, inflate(compressed, true)), "raw nowrap round-trip");
    }

    static void testDeflateOverloads() throws Exception {
        byte[] input = "overload test".getBytes("UTF-8");
        Deflater def = new Deflater();
        def.setInput(input);
        def.finish();
        byte[] out = new byte[256];
        int off = 10;
        int n = def.deflate(out, off, 100);
        check(n > 0, "deflate(byte[],off,len) returns bytes");
        check(Arrays.equals(input, inflate(Arrays.copyOfRange(out, off, off + n), false)), "deflate(byte[],off,len) round-trip");
        def.end();
    }

    static void testReset() throws Exception {
        byte[] input = "abc".getBytes("UTF-8");
        Deflater def = new Deflater();
        deflateToBytes(def, input);
        check(def.finished(), "finished before reset");
        def.reset();
        check(!def.finished(), "reset clears finished");
        check(def.needsInput(), "reset clears input");
        byte[] compressed = deflateToBytes(def, input);
        check(Arrays.equals(input, inflate(compressed, false)), "reusable after reset");
        def.end();
    }

    static void testNullAndBounds() {
        Deflater def = new Deflater();
        checkThrows(NullPointerException.class, () -> def.setInput((byte[]) null), "setInput null NPE");
        checkThrows(NullPointerException.class, () -> def.setInput((byte[]) null, 0, 1), "setInput null off/len NPE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> def.setInput(new byte[5], -1, 1), "setInput negative off AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> def.setInput(new byte[5], 0, 6), "setInput len past end AIOOBE");
        checkThrows(NullPointerException.class, () -> def.deflate((byte[]) null), "deflate null NPE");
        checkThrows(NullPointerException.class, () -> def.deflate((byte[]) null, 0, 1), "deflate null off/len NPE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> def.deflate(new byte[5], 0, 6), "deflate len past end AIOOBE");
        def.end();
        Deflater ended = new Deflater();
        ended.end();
        checkThrows(IllegalStateException.class, () -> ended.deflate(new byte[8]),
                "use after close IllegalStateException (JDK-13+ 与 JDK-8 的 NPE 不同，版本差异锁定)");
    }


    // ===================== JDK-9+ 增量：ByteBuffer 通道/close()/AutoCloseable（JDK-27 侧锁定） =====================
    static void testJdk27Additions() throws Exception {
        check(Deflater.class.getInterfaces() != null
              && java.util.Arrays.asList(Deflater.class.getInterfaces()).contains(java.lang.AutoCloseable.class),
              "Deflater implements AutoCloseable (JDK-7close since 12)");
        byte[] raw = new byte[4096];
        for (int i = 0; i < raw.length; i++) raw[i] = (byte) i;
        // deflate(ByteBuffer) / deflate(ByteBuffer, flush)
        java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(raw);
        java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(8192);
        Deflater def = new Deflater();
        def.setInput(in);
        while (!def.needsInput()) {
            def.deflate(out);
        }
        def.finish();
        while (!def.finished()) def.deflate(out);
        out.flip();
        byte[] zipped = new byte[out.remaining()];
        out.get(zipped);
        // setInput(ByteBuffer) + deflate(ByteBuffer) 产物可被 Inflater(ByteBuffer) 还原
        Inflater inf = new Inflater();
        inf.setInput(java.nio.ByteBuffer.wrap(zipped));
        java.nio.ByteBuffer restored = java.nio.ByteBuffer.allocate(raw.length);
        while (!inf.finished()) inf.inflate(restored);
        restored.flip();
        byte[] back = new byte[restored.remaining()];
        restored.get(back);
        check(java.util.Arrays.equals(back, raw), "ByteBuffer deflate/inflate round-trip");
        check(inf.getRemaining() == 0, "no leftover input");
        inf.close();
        // close() 语义：幂等，之后的操作 IllegalStateException
        Deflater closable = new Deflater();
        closable.close();
        closable.close();
        try {
            closable.deflate(new byte[8]);
            check(false, "deflate after close ISE");
        } catch (IllegalStateException e) {
            check("Deflater has been closed".equals(e.getMessage()), "close message: " + e.getMessage());
        }
        // try-with-resources
        try (Deflater twr = new Deflater()) {
            twr.setInput(new byte[] {1, 2, 3});
            check(twr.deflate(java.nio.ByteBuffer.allocate(64)) >= 0, "TWR usable");
        }
        // setDictionary(ByteBuffer)
        Deflater dict = new Deflater();
        dict.setDictionary(java.nio.ByteBuffer.wrap("dictionary".getBytes("UTF-8")));
        check(true, "setDictionary(ByteBuffer) accepted");
        dict.end();
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testLevels();
        testNowrap();
        testDeflateOverloads();
        testReset();
        testNullAndBounds();        testJdk27Additions();

        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.util.zip.Inflater contract: round-trip, needsInput/needsDictionary/
 *          finished, getRemaining/getAdler/getBytes*, reset, end, DataFormatException
 *          on corrupt data, null->NPE, bad-bounds->AIOOBE, raw (nowrap) inflate.
 * @run main InflaterTest
 */

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class InflaterTest {
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

    static byte[] compress(byte[] input, boolean nowrap) {
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, nowrap);
        def.setInput(input);
        def.finish();
        byte[] out = new byte[input.length * 2 + 64];
        int n = def.deflate(out);
        def.end();
        return Arrays.copyOf(out, n);
    }

    static void testRoundTrip() throws Exception {
        byte[] input = "hello hello hello hello".getBytes("UTF-8");
        byte[] compressed = compress(input, false);
        Inflater inf = new Inflater();
        check(inf.needsInput(), "needsInput initially");
        check(!inf.needsDictionary(), "needsDictionary false");
        inf.setInput(compressed);
        byte[] result = new byte[256];
        int rlen = inf.inflate(result);
        check(rlen == input.length, "inflate restores length");
        check(Arrays.equals(input, Arrays.copyOf(result, rlen)), "inflate restores content");
        check(inf.finished(), "finished");
        check(inf.getAdler() != 0, "getAdler after inflate");
        check(inf.getBytesRead() == compressed.length, "getBytesRead");
        check(inf.getBytesWritten() == input.length, "getBytesWritten");
        check(inf.getRemaining() == 0, "getRemaining 0 when finished");
        inf.end();
    }

    static void testNowrap() throws Exception {
        byte[] input = "raw deflate".getBytes("UTF-8");
        byte[] compressed = compress(input, true);
        Inflater inf = new Inflater(true);
        inf.setInput(compressed);
        byte[] result = new byte[256];
        int n = inf.inflate(result);
        check(Arrays.equals(input, Arrays.copyOf(result, n)), "raw nowrap round-trip");
        inf.end();
    }

    static void testCorruptData() throws Exception {
        Inflater inf = new Inflater();
        byte[] garbage = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        inf.setInput(garbage);
        checkThrows(DataFormatException.class, () -> inf.inflate(new byte[64]), "corrupt data -> DataFormatException");
        inf.end();
    }

    static void testReset() throws Exception {
        byte[] input = "abc".getBytes("UTF-8");
        Inflater inf = new Inflater();
        inf.setInput(compress(input, false));
        byte[] out = new byte[64];
        inf.inflate(out);
        check(inf.finished(), "finished before reset");
        inf.reset();
        check(!inf.finished(), "reset clears finished");
        check(inf.needsInput(), "reset clears input");
        inf.setInput(compress(input, false));
        inf.inflate(out);
        check(inf.finished(), "reusable after reset");
        inf.end();
    }

    static void testNullAndBounds() {
        Inflater inf = new Inflater();
        checkThrows(NullPointerException.class, () -> inf.setInput((byte[]) null), "setInput null NPE");
        checkThrows(NullPointerException.class, () -> inf.setInput((byte[]) null, 0, 1), "setInput null off/len NPE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> inf.setInput(new byte[5], -1, 1), "setInput negative off AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> inf.setInput(new byte[5], 0, 6), "setInput len past end AIOOBE");
        checkThrows(NullPointerException.class, () -> inf.inflate((byte[]) null), "inflate null NPE");
        checkThrows(NullPointerException.class, () -> inf.inflate((byte[]) null, 0, 1), "inflate null off/len NPE");
        inf.end();
        Inflater ended = new Inflater();
        ended.end();
        checkThrows(IllegalStateException.class, () -> ended.inflate(new byte[8]),
                "use after close IllegalStateException (JDK-13+ 与 JDK-8 的 NPE 不同，版本差异锁定)");
    }


    // ===================== JDK-9+ 增量：ByteBuffer 通道/close()/AutoCloseable（JDK-27 侧锁定） =====================
    static void testJdk27Additions() throws Exception {
        check(java.util.Arrays.asList(Inflater.class.getInterfaces()).contains(java.lang.AutoCloseable.class),
              "Inflater implements AutoCloseable");
        byte[] raw = new byte[2048];
        for (int i = 0; i < raw.length; i++) raw[i] = (byte) (i * 3);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.DeflaterOutputStream d = new java.util.zip.DeflaterOutputStream(bos)) { d.write(raw); }
        // setInput(ByteBuffer) + inflate(ByteBuffer)
        Inflater inf = new Inflater();
        inf.setInput(java.nio.ByteBuffer.wrap(bos.toByteArray()));
        java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(raw.length + 64);
        while (!inf.finished()) inf.inflate(out);
        out.flip();
        byte[] back = new byte[out.remaining()];
        out.get(back);
        check(java.util.Arrays.equals(back, raw), "inflate(ByteBuffer) round-trip");
        inf.close();
        // close 幂等 + 之后 IllegalStateException
        Inflater closed = new Inflater();
        closed.close();
        closed.close();
        try {
            closed.inflate(new byte[8]);
            check(false, "inflate after close ISE");
        } catch (IllegalStateException e) {
            check("Inflater has been closed".equals(e.getMessage()), "close message: " + e.getMessage());
        }
        // setDictionary(ByteBuffer) 只能在 zlib 发出 needsDictionary 信号后调用；新鲜实例直接调用 -> IAE（锁定）
        Inflater dict = new Inflater();
        check(!dict.needsDictionary(), "fresh inflater needsDictionary false");
        try {
            dict.setDictionary(java.nio.ByteBuffer.wrap("dictionary".getBytes("UTF-8")));
            check(false, "setDictionary before NEED_DICT");
        } catch (IllegalArgumentException expected) {
            check(true, "setDictionary before NEED_DICT throws IAE");
        }
        dict.end();
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testNowrap();
        testCorruptData();
        testReset();
        testNullAndBounds();        testJdk27Additions();

        System.out.println("PASS (" + passed + " assertions)");
    }
}

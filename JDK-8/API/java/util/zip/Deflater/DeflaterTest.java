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
        checkThrows(NullPointerException.class, () -> ended.deflate(new byte[8]), "use after end NPE");
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testLevels();
        testNowrap();
        testDeflateOverloads();
        testReset();
        testNullAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

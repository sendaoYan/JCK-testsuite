/*
 * @test
 * @summary java.util.zip.Deflater conformance: white-box structure + black-box deflate round-trip.
 * @run main DeflaterTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.Deflater");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");

        check(Deflater.DEFLATED == 8, "DEFLATED == 8");
        check(Deflater.NO_COMPRESSION == 0, "NO_COMPRESSION == 0");
        check(Deflater.BEST_SPEED == 1, "BEST_SPEED == 1");
        check(Deflater.BEST_COMPRESSION == 9, "BEST_COMPRESSION == 9");
        check(Deflater.DEFAULT_COMPRESSION == -1, "DEFAULT_COMPRESSION == -1");
        check(Deflater.HUFFMAN_ONLY == 2, "HUFFMAN_ONLY == 2");
        check(Deflater.DEFAULT_STRATEGY == 0, "DEFAULT_STRATEGY == 0");

        c.getConstructor(int.class, boolean.class);
        c.getConstructor(int.class);
        c.getConstructor();
        c.getMethod("setInput", byte[].class, int.class, int.class);
        c.getMethod("setInput", byte[].class);
        c.getMethod("setInput", ByteBuffer.class);
        c.getMethod("setDictionary", byte[].class);
        c.getMethod("setStrategy", int.class);
        c.getMethod("setLevel", int.class);
        c.getMethod("needsInput");
        c.getMethod("finish");
        c.getMethod("finished");
        c.getMethod("deflate", byte[].class);
        c.getMethod("deflate", byte[].class, int.class, int.class);
        c.getMethod("deflate", ByteBuffer.class);
        c.getMethod("getAdler");
        c.getMethod("getBytesRead");
        c.getMethod("getBytesWritten");
        c.getMethod("reset");
        c.getMethod("end");
        check(true, "constructors and key methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] input = "hello hello hello hello".getBytes("UTF-8");

        Deflater d = new Deflater();
        check(!d.finished(), "not finished initially");
        check(d.needsInput(), "needsInput when empty");
        d.setInput(input);
        check(!d.needsInput(), "!needsInput after setInput");
        d.finish();
        byte[] out = new byte[256];
        int clen = d.deflate(out);
        check(clen > 0, "deflate produced output");
        check(d.finished(), "finished after deflate");
        check(d.getBytesRead() == input.length, "getBytesRead");
        check(d.getBytesWritten() > 0, "getBytesWritten");
        check(d.getTotalIn() == input.length, "getTotalIn");
        check(d.getTotalOut() > 0, "getTotalOut");
        check(d.getAdler() != 0, "getAdler");
        d.reset();
        check(d.getBytesRead() == 0 && d.getBytesWritten() == 0, "reset clears counters");
        d.end();

        // round-trip via Inflater
        Deflater d2 = new Deflater(Deflater.BEST_COMPRESSION);
        d2.setInput(input);
        d2.finish();
        byte[] comp = new byte[512];
        int cl2 = d2.deflate(comp);
        d2.end();
        Inflater inf = new Inflater();
        inf.setInput(comp, 0, cl2);
        byte[] result = new byte[512];
        int rl = inf.inflate(result);
        check(rl == input.length && Arrays.equals(input, Arrays.copyOf(result, rl)), "deflate/inflate round-trip");
        inf.end();

        // setLevel/setStrategy + deflate(byte[],off,len); HUFFMAN_ONLY may need several passes
        Deflater d3 = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
        d3.setLevel(Deflater.BEST_SPEED);
        d3.setStrategy(Deflater.HUFFMAN_ONLY);
        d3.setInput(input);
        d3.finish();
        byte[] comp3 = new byte[512];
        int cl3 = 0;
        while (!d3.finished()) {
            cl3 += d3.deflate(comp3, cl3, comp3.length - cl3);
        }
        check(cl3 > 0, "deflate(byte[],off,len) produced output");
        d3.end();

        // setInput(ByteBuffer) + deflate(ByteBuffer)
        Deflater d5 = new Deflater();
        d5.setInput(ByteBuffer.wrap(input));
        d5.finish();
        ByteBuffer compBuf = ByteBuffer.allocate(512);
        int cl5 = d5.deflate(compBuf);
        check(cl5 > 0, "setInput(ByteBuffer)/deflate(ByteBuffer) produced output");
        d5.end();

        // preset dictionary round-trip (mirrors test/jdk DeflaterDictionaryTests)
        byte[] dict = "0123456789dictionary".getBytes("UTF-8");
        Deflater d4 = new Deflater();
        d4.setDictionary(dict);
        d4.setInput(input);
        d4.finish();
        byte[] comp4 = new byte[512];
        int cl4 = d4.deflate(comp4);
        d4.end();
        Inflater inf4 = new Inflater();
        inf4.setInput(comp4, 0, cl4);
        byte[] res4 = new byte[512];
        inf4.inflate(res4);
        check(inf4.needsDictionary(), "needsDictionary for dictionary-compressed data");
        inf4.setDictionary(dict);
        int r4 = inf4.inflate(res4);
        check(r4 == input.length && Arrays.equals(input, Arrays.copyOf(res4, r4)), "preset dictionary round-trip");
        inf4.end();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

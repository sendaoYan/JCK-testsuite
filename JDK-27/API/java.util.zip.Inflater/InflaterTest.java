/*
 * @test
 * @summary java.util.zip.Inflater conformance: white-box structure + black-box inflate round-trip.
 * @run main InflaterTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class InflaterTest {
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
        Class<?> c = clazz("java.util.zip.Inflater");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");

        c.getConstructor(boolean.class);
        c.getConstructor();
        c.getMethod("setInput", byte[].class, int.class, int.class);
        c.getMethod("setInput", byte[].class);
        c.getMethod("setDictionary", byte[].class);
        c.getMethod("getRemaining");
        c.getMethod("needsInput");
        c.getMethod("needsDictionary");
        c.getMethod("finished");
        c.getMethod("inflate", byte[].class);
        c.getMethod("inflate", byte[].class, int.class, int.class);
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
        Deflater def = new Deflater();
        def.setInput(input);
        def.finish();
        byte[] compressed = new byte[256];
        int clen = def.deflate(compressed);
        def.end();

        Inflater inf = new Inflater();
        check(inf.needsInput(), "needsInput initially");
        check(!inf.needsDictionary(), "needsDictionary false initially");
        inf.setInput(compressed, 0, clen);
        byte[] result = new byte[256];
        int rlen = inf.inflate(result);
        check(rlen == input.length, "inflate restores length");
        check(Arrays.equals(input, Arrays.copyOf(result, rlen)), "inflate restores content");
        check(inf.finished(), "finished");
        check(inf.getAdler() != 0, "getAdler");
        check(inf.getBytesRead() == clen, "getBytesRead");
        check(inf.getBytesWritten() == input.length, "getBytesWritten");
        check(inf.getTotalIn() == clen, "getTotalIn");
        check(inf.getTotalOut() == input.length, "getTotalOut");
        check(inf.getRemaining() == 0, "getRemaining 0 when finished");
        inf.reset();
        check(inf.needsInput() && !inf.finished(), "reset returns to initial state");
        inf.end();

        // nowrap round-trip
        Deflater nd = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        nd.setInput(input);
        nd.finish();
        byte[] nc = new byte[256];
        int ncl = nd.deflate(nc);
        nd.end();
        Inflater ni = new Inflater(true);
        ni.setInput(nc, 0, ncl);
        byte[] nr = new byte[256];
        int nrl = ni.inflate(nr);
        check(nrl == input.length && Arrays.equals(input, Arrays.copyOf(nr, nrl)), "nowrap round-trip");
        ni.end();

        // inflate(byte[],off,len)
        Deflater d2 = new Deflater();
        d2.setInput(input);
        d2.finish();
        byte[] c2 = new byte[256];
        int c2l = d2.deflate(c2);
        d2.end();
        Inflater i2 = new Inflater();
        i2.setInput(c2, 0, c2l);
        byte[] r2 = new byte[256];
        int r2l = i2.inflate(r2, 0, r2.length);
        check(r2l == input.length, "inflate(byte[],off,len) restores length");
        check(Arrays.equals(input, Arrays.copyOf(r2, r2l)), "inflate(byte[],off,len) restores content");
        i2.end();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

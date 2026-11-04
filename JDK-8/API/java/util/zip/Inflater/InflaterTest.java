/*
 * @test
 * @summary java.util.zip.Inflater conformance: round-trip inflate.
 * @run main InflaterTest
 */

import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class InflaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInflater() throws Exception {
        byte[] input = "hello hello hello hello".getBytes("UTF-8");
        Deflater def = new Deflater();
        def.setInput(input);
        def.finish();
        byte[] compressed = new byte[256];
        int clen = def.deflate(compressed);
        def.end();

        Inflater inf = new Inflater();
        check(inf.needsInput(), "needsInput initially");
        check(!inf.needsDictionary(), "needsDictionary false");
        inf.setInput(compressed, 0, clen);
        byte[] result = new byte[256];
        int rlen = inf.inflate(result);
        check(rlen == input.length, "inflate restores length");
        check(Arrays.equals(input, Arrays.copyOf(result, rlen)), "inflate restores content");
        check(inf.finished(), "finished");
        check(inf.getAdler() != 0, "getAdler");
        check(inf.getBytesRead() == clen, "getBytesRead");
        check(inf.getBytesWritten() == input.length, "getBytesWritten");
        check(inf.getRemaining() == 0, "getRemaining 0 when finished");
        inf.end();
    }

    public static void main(String[] args) throws Exception {
        testInflater();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

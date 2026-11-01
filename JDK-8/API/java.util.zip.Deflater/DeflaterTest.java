/*
 * @test
 * @summary java.util.zip.Deflater conformance: deflate/needsInput/finish/bytes.
 * @run main DeflaterTest
 */

import java.util.zip.Deflater;

public class DeflaterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeflater() throws Exception {
        byte[] input = "hello hello hello hello".getBytes("UTF-8");
        Deflater d = new Deflater();
        check(!d.finished(), "not finished initially");
        check(d.needsInput(), "needsInput when empty");
        d.setInput(input);
        check(!d.needsInput(), "!needsInput after setInput");
        d.finish();
        byte[] out = new byte[256];
        int compressed = d.deflate(out);
        check(compressed > 0, "deflate produced output");
        check(d.finished(), "finished after deflate");
        check(d.getBytesRead() == input.length, "getBytesRead");
        check(d.getBytesWritten() > 0, "getBytesWritten");
        check(d.getAdler() != 0, "getAdler");
        d.reset();
        check(d.getBytesRead() == 0 && d.getBytesWritten() == 0, "reset clears counters");
        d.end();

        Deflater d2 = new Deflater(Deflater.BEST_COMPRESSION);
        d2.setLevel(Deflater.BEST_SPEED);
        d2.setStrategy(Deflater.HUFFMAN_ONLY);
        d2.end();
        check(true, "setLevel/setStrategy accept valid values");
    }

    public static void main(String[] args) throws Exception {
        testDeflater();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

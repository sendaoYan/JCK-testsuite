/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.BitArray conformance: behavioral construction.
 * @run main BitArrayTest
 */

public class BitArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.xalan.internal.xsltc.dom.BitArray b = new com.sun.org.apache.xalan.internal.xsltc.dom.BitArray(64);
        check(b.size() == 64, "size 64");
        check(!b.getBit(3), "bit 3 initially unset");
        b.setBit(3);
        check(b.getBit(3), "setBit/getBit");
        check(!b.getBit(4), "bit 4 still unset");
        check(b.data().length >= 2, "data length");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

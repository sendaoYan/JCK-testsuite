/*
 * @test
 * @summary java.net.IDN conformance: toASCII/toUnicode.
 * @run main IDNTest
 */


public class IDNTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        String ascii = java.net.IDN.toASCII("b\u00fccher.de");
        check("xn--bcher-kva.de".equals(ascii), "toASCII");
        check("b\u00fccher.de".equals(java.net.IDN.toUnicode(ascii)), "toUnicode");
        check("a-b-c".equals(java.net.IDN.toASCII("a-b-c", java.net.IDN.USE_STD3_ASCII_RULES)), "USE_STD3_ASCII_RULES");        System.out.println("PASS (" + passed + " assertions)");
    }
}
